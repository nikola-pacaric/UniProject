package com.example.uniproject.ui.categories;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uniproject.R;
import com.example.uniproject.data.model.category.CategoryResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CategoryAdapter
        extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    private final OnCategoryEditClickListener editClickListener;
    private final OnCategoryDeleteClickListener deleteClickListener;
    private List<CategoryResponse> categories = Collections.emptyList();
    private boolean actionsEnabled = true;

    public CategoryAdapter(
            OnCategoryEditClickListener editClickListener,
            OnCategoryDeleteClickListener deleteClickListener
    ) {
        this.editClickListener = editClickListener;
        this.deleteClickListener = deleteClickListener;
        setHasStableIds(true);
    }

    public void submitList(List<CategoryResponse> categories) {
        this.categories = categories == null
                ? Collections.emptyList()
                : new ArrayList<>(categories);
        notifyDataSetChanged();
    }

    public void setActionsEnabled(boolean enabled) {
        if (actionsEnabled == enabled) {
            return;
        }
        actionsEnabled = enabled;
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        Long id = categories.get(position).getId();
        return id == null ? RecyclerView.NO_ID : id;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        holder.bind(
                categories.get(position),
                actionsEnabled,
                editClickListener,
                deleteClickListener
        );
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static final class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameText;
        private final TextView descriptionText;
        private final ImageButton editButton;
        private final ImageButton deleteButton;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.categoryNameText);
            descriptionText = itemView.findViewById(R.id.categoryDescriptionText);
            editButton = itemView.findViewById(R.id.editCategoryButton);
            deleteButton = itemView.findViewById(R.id.deleteCategoryButton);
        }

        void bind(
                CategoryResponse category,
                boolean actionsEnabled,
                OnCategoryEditClickListener editClickListener,
                OnCategoryDeleteClickListener deleteClickListener
        ) {
            String name = category.getName() == null ? "" : category.getName();
            nameText.setText(name);

            String description = category.getDescription();
            descriptionText.setText(TextUtils.isEmpty(description)
                    ? itemView.getContext().getString(R.string.category_description_missing)
                    : description);

            editButton.setContentDescription(itemView.getContext().getString(
                    R.string.edit_category_content_description,
                    name
            ));
            editButton.setEnabled(actionsEnabled);
            editButton.setOnClickListener(ignored -> editClickListener.onEditClick(category));

            deleteButton.setContentDescription(itemView.getContext().getString(
                    R.string.delete_category_content_description,
                    name
            ));
            deleteButton.setEnabled(actionsEnabled);
            deleteButton.setOnClickListener(ignored -> deleteClickListener.onDeleteClick(category));
        }
    }

    public interface OnCategoryEditClickListener {
        void onEditClick(CategoryResponse category);
    }

    public interface OnCategoryDeleteClickListener {
        void onDeleteClick(CategoryResponse category);
    }
}
