package com.zacharee1.aospsignboard.configure;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.SignBoardManager;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.jaredrummler.android.colorpicker.ColorPreference;
import com.zacharee1.aospsignboard.App;
import com.zacharee1.aospsignboard.PageEditorActivity;
import com.zacharee1.aospsignboard.R;
import com.zacharee1.aospsignboard.widgets.QuickToggles;
import com.zacharee1.aospsignboard.widgets.helpers.QTIcon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class QuickTogglesConfigure extends AppCompatActivity {
    private Adapter enabledAdapter;
    private Adapter disabledAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_quicktoggles_configure);
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.prefs_holder, new Prefs())
                .commit();

        enabledAdapter = new Adapter(this, i -> disabledAdapter.addItem(i));
        disabledAdapter = new Adapter(this, i -> enabledAdapter.addItem(i));
        disabledAdapter.setShouldRemoveOnTap(true);
        disabledAdapter.setShouldSaveChanges(false);

        RecyclerView recyclerView = findViewById(R.id.recycler);
        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                return makeMovementFlags(ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, ItemTouchHelper.DOWN | ItemTouchHelper.UP);
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return enabledAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                enabledAdapter.removeItem(viewHolder.getAdapterPosition());
            }

            @Override
            public boolean isLongPressDragEnabled() {
                return true;
            }

            @Override
            public boolean isItemViewSwipeEnabled() {
                return true;
            }
        });

        recyclerView.setAdapter(enabledAdapter);
        recyclerView.setHasFixedSize(true);
        helper.attachToRecyclerView(recyclerView);

        RecyclerView disabled = findViewById(R.id.avail);
        disabled.setAdapter(disabledAdapter);

        App.get(this)
                .getToggleList()
                .stream()
                .map(k -> QTIcon.getInstance(this, k))
                .forEach(enabledAdapter::addItem);

        recyclerView.setLayoutManager(new GridLayoutManager(this, enabledAdapter.getItemCount()) {
            @Override
            public void onItemsAdded(RecyclerView recyclerView, int positionStart, int itemCount) {
                int count = enabledAdapter.getItemCount();
                setSpanCount(count == 0 ? 1 : count);
                super.onItemsAdded(recyclerView, positionStart, itemCount);
            }

            @Override
            public void onItemsRemoved(RecyclerView recyclerView, int positionStart, int itemCount) {
                int count = enabledAdapter.getItemCount();
                setSpanCount(count == 0 ? 1 : count);
                super.onItemsRemoved(recyclerView, positionStart, itemCount);
            }

            @Override
            public boolean canScrollHorizontally() {
                return false;
            }

            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });

        Arrays.stream(SignBoardManager.QT_ITEMS)
                .map(k -> QTIcon.getInstance(this, k))
                .filter(i -> !enabledAdapter.icons.contains(i))
                .forEach(disabledAdapter::addItem);

        disabled.setLayoutManager(new GridLayoutManager(this, disabledAdapter.getItemCount()) {
            @Override
            public void onItemsAdded(RecyclerView recyclerView, int positionStart, int itemCount) {
                int count = disabledAdapter.getItemCount();
                setSpanCount(count == 0 ? 1 : count);
                super.onItemsAdded(recyclerView, positionStart, itemCount);
            }

            @Override
            public void onItemsRemoved(RecyclerView recyclerView, int positionStart, int itemCount) {
                int count = disabledAdapter.getItemCount();
                setSpanCount(count == 0 ? 1 : count);
                super.onItemsRemoved(recyclerView, positionStart, itemCount);
            }

            @Override
            public boolean canScrollHorizontally() {
                return false;
            }

            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
    }

    private static class Adapter extends RecyclerView.Adapter<Adapter.VH> implements PageEditorActivity.ItemTouchHelperAdapter {
        ArrayList<QTIcon> icons = new ArrayList<>();

        private ArrayList<ImageView> toTint = new ArrayList<>();

        private Context context;
        private ItemChangeNotifier notifier;
        private boolean shouldRemoveOnTap = false;
        private boolean shouldSaveChanges = true;

        private SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener = (sharedPreferences, key) -> {
            if (key.contains("_color")) {
                toTint.stream()
                        .filter(v -> ((QTIcon) v.getTag()).colorKey.equals(key))
                        .forEach(v -> {
                            QTIcon icon = (QTIcon) v.getTag();
                            v.setImageTintList(ColorStateList.valueOf(icon.getColor()));
                        });
            }
        };

        public Adapter(Context context, ItemChangeNotifier notifier) {
            this.context = context;
            this.notifier = notifier;

            PreferenceManager.getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
        }

        public void addItem(QTIcon icon) {
            icons.add(icon);
            notifyItemInserted(icons.size() - 1);
            saveIcons();
        }

        public void removeItem(int position) {
            notifier.onItemRemoved(icons.remove(position));
            notifyItemRemoved(position);
            saveIcons();
        }

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View item = LayoutInflater.from(context).inflate(R.layout.qt_item, parent, false);
            return new VH(item);
        }

        @Override
        public void onBindViewHolder(VH holder, int position) {
            QTIcon icon = icons.get(position);
            ImageView view = (ImageView) LayoutInflater.from(context).inflate(icon.getLayoutId(), (ViewGroup) holder.itemView, false);
            view.setImageResource(icon.getDrawableId());
            view.setImageTintList(ColorStateList.valueOf(icon.getColor()));
            view.setTag(icon);
            view.setOnClickListener(v -> {
                if (shouldRemoveOnTap) removeItem(holder.getAdapterPosition());
            });
            toTint.add(view);
            holder.setView(view);
        }

        @Override
        public int getItemCount() {
            return icons.size();
        }

        @Override
        public boolean onItemMove(int fromPosition, int toPosition) {
            icons.add(toPosition, icons.remove(fromPosition));
            saveIcons();
            notifyItemMoved(fromPosition, toPosition);
            return true;
        }

        public void setShouldRemoveOnTap(boolean shouldRemoveOnTap) {
            this.shouldRemoveOnTap = shouldRemoveOnTap;
        }

        public void setShouldSaveChanges(boolean shouldSaveChanges) {
            this.shouldSaveChanges = shouldSaveChanges;
        }

        private void saveIcons() {
            if (shouldSaveChanges) {
                App.get(context).saveToggleList(icons.stream().map(i -> i.key).collect(Collectors.toCollection(ArrayList::new)));

                updateWidget();
            }
        }

        private void updateWidget() {
            Intent update = new Intent(SignBoardManager.ACTION_UPDATE_QUICKTOGGLES);
            update.setComponent(new ComponentName(context, QuickToggles.class));
            context.sendBroadcast(update);
        }

        static class VH extends RecyclerView.ViewHolder {
            public VH(View view) {
                super(view);
            }

            public void setView(View view) {
                ((ViewGroup) itemView).removeAllViews();
                ((ViewGroup) itemView).addView(view);
            }
        }
    }

    public static class Prefs extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.prefs_qs);
            addAllToggles();
        }

        private void addAllToggles() {
            for (String key : SignBoardManager.QT_ITEMS) {
                QTIcon icon = QTIcon.getInstance(getActivity(), key);
                ColorPreference preference = (ColorPreference) getPreferenceManager()
                        .inflateFromResource(getActivity(), R.xml.base_color_pref, null)
                        .findPreference("base");
                preference.setKey(icon.colorKey);
                preference.setTitle(icon.getTitle());
                preference.setOnPreferenceChangeListener((pref, newValue) -> {
                    QuickToggles.update(getActivity(), key);
                    return true;
                });
                getPreferenceScreen().addPreference(preference);
            }
        }
    }

    @FunctionalInterface
    private interface ItemChangeNotifier {
        void onItemRemoved(QTIcon icon);
    }
}