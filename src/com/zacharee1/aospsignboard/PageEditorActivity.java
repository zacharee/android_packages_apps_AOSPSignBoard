package com.zacharee1.aospsignboard;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PageEditorActivity extends Activity {
    private RecyclerView recyclerView;
    private Adapter adapter;
    private ItemTouchHelper helper;

    private ArrayList<ComponentName> enabledWidgets = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout_editor);

        adapter = new Adapter(parseWidgets(), v -> helper.startDrag(v), new WidgetListener() {
            @Override
            public void onEnableStateChanged(boolean enabled, ComponentName componentName, int position) {
                removeOrAddWidget(componentName, enabled, position);
            }

            @Override
            public void onPositionChanged(int from, int to, ComponentName componentName) {
                moveWidget(componentName, from, to);
            }
        });

        ItemTouchHelper.Callback callback = new Callback(adapter);
        helper = new ItemTouchHelper(callback);

        recyclerView = findViewById(R.id.recycler);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayout.VERTICAL, false));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayout.VERTICAL));

        helper.attachToRecyclerView(recyclerView);
    }

    private ArrayList<ItemView.Info> parseWidgets() {
        ArrayList<ItemView.Info> ret = new ArrayList<>();
        List<AppWidgetProviderInfo> avail =
                AppWidgetManager.getInstance(this)
                        .getInstalledProviders(Resources.getSystem().getInteger(com.android.internal.R.integer.config_signBoardCategory));
        String storedEnabled = Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_SIGNBOARD_COMPONENTS);
        if (storedEnabled == null || storedEnabled.isEmpty()) enabledWidgets = new ArrayList<>();
        else enabledWidgets =
                Arrays.stream(storedEnabled.split(";"))
                        .map(s -> {
                            String[] split = s.split("/");
                            return new ComponentName(split[0], split[1]);
                        }).collect(Collectors.toCollection(ArrayList::new));

        avail.forEach(i -> {
            ItemView.Info info = new ItemView.Info();
            info.component = i.provider;
            info.configure = i.configure;
            info.enabled = enabledWidgets.contains(i.provider);
            info.title = i.loadLabel(getPackageManager());
            ret.add(info);
        });

        ArrayList<ItemView.Info> newRet = ret.stream()
                .filter(i -> i.enabled).collect(Collectors.toCollection(ArrayList::new));
        ret.removeAll(newRet);

        for (int i = 0; i < enabledWidgets.size(); i++) {
            ret.add(i, newRet.get(i));
        }

        return ret;
    }

    private void moveWidget(ComponentName widget, int from, int to) {
        ComponentName w = enabledWidgets.remove(from);
        enabledWidgets.add(to, w);
        saveList(enabledWidgets);
    }

    private void removeOrAddWidget(ComponentName widget, boolean enabled, int position) {
        if (enabled) enabledWidgets.add(position, widget);
        else enabledWidgets.remove(widget);
        saveList(enabledWidgets);
    }

    private void saveList(ArrayList<ComponentName> widgets) {
        Settings.Secure.putString(getContentResolver(),
                Settings.Secure.ENABLED_SIGNBOARD_COMPONENTS,
                TextUtils.join(";", widgets.stream().map(ComponentName::flattenToString).collect(Collectors.toCollection(ArrayList::new))));
    }

    public static class Adapter extends RecyclerView.Adapter<Adapter.VH> implements ItemTouchHelperAdapter {
        private OnStartDragListener listener;
        private WidgetListener widgetListener;
        private ArrayList<ItemView.Info> infos = new ArrayList<>();

        public Adapter(ArrayList<ItemView.Info> infos, OnStartDragListener listener, WidgetListener widgetListener) {
            this.infos.addAll(infos);
            this.listener = listener;
            this.widgetListener = widgetListener;
        }

        @Override
        public boolean onItemMove(int fromPosition, int toPosition) {
            ItemView.Info info = infos.remove(fromPosition);
            infos.add(toPosition, info);

            if (info.enabled) widgetListener.onPositionChanged(fromPosition, toPosition, info.component);

            notifyItemMoved(fromPosition, toPosition);
            return true;
        }

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            return new VH(new ItemView(parent.getContext()));
        }

        @Override
        public void onBindViewHolder(VH holder, int position) {
            ItemView.Info info = infos.get(position);
            holder.setTitle(info.title);
            holder.setEnabled(info.enabled);

            holder.getHandle().setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    listener.onStartDrag(holder);
                }
                return true;
            });

            ((ItemView) holder.itemView).toggle.setOnCheckedChangeListener((button, checked) -> {
                widgetListener.onEnableStateChanged(checked, info.component, holder.getAdapterPosition());
                info.enabled = checked;
            });
        }

        @Override
        public int getItemCount() {
            return infos.size();
        }

        static class VH extends RecyclerView.ViewHolder {
            public VH(ItemView view) {
                super(view);
            }

            public void setTitle(String title) {
                ((ItemView) itemView).name.setText(title);
            }

            public void setEnabled(boolean enabled) {
                ((ItemView) itemView).toggle.setChecked(enabled);
            }

            public ImageView getHandle() {
                return ((ItemView) itemView).handle;
            }
        }
    }

    public class Callback extends ItemTouchHelper.Callback {
        private ItemTouchHelperAdapter adapter;

        public Callback(ItemTouchHelperAdapter adapter) {
            this.adapter = adapter;
        }

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return adapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

        }

        @Override
        public boolean isLongPressDragEnabled() {
            return true;
        }

        @Override
        public boolean isItemViewSwipeEnabled() {
            return false;
        }
    }

    public interface OnStartDragListener {

        /**
         * Called when a view is requesting a start of a drag.
         *
         * @param viewHolder The holder of the view to drag.
         */
        void onStartDrag(RecyclerView.ViewHolder viewHolder);
    }

    public interface WidgetListener {
        void onEnableStateChanged(boolean enabled, ComponentName componentName, int position);
        void onPositionChanged(int from, int to, ComponentName componentName);
    }

    public interface ItemTouchHelperAdapter {
        boolean onItemMove(int fromPosition, int toPosition);
    }
}