package com.example.uniproject.ui.categories;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.uniproject.R;

public final class CategoriesFragment extends Fragment {
    public CategoriesFragment() {
        super(R.layout.fragment_feature_placeholder);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView description = view.findViewById(R.id.featureDescriptionText);
        description.setText(R.string.categories_description);
    }
}
