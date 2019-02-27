package org.dhis2.usescases.datasets.dataSetTable;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.dhis2.R;
import org.dhis2.databinding.ItemDatasetBinding;
import org.dhis2.databinding.ItemTableCheckboxBinding;
import org.dhis2.usescases.datasets.datasetDetail.DataSetDetailContract;
import org.dhis2.usescases.datasets.datasetDetail.DataSetDetailModel;
import org.dhis2.usescases.datasets.datasetDetail.DataSetDetailViewHolder;
import org.dhis2.usescases.datasets.datasetDetail.DataSetDiffCallback;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

public class TableCheckboxAdapter extends RecyclerView.Adapter<TableCheckboxViewHolder> {

    private List<String> tables;
    private DataSetTableContract.Presenter presenter;

    public TableCheckboxAdapter(DataSetTableContract.Presenter presenter){
        this.tables = new ArrayList<>();
        this.presenter = presenter;
    }

    public void swapData(List<String> tables){
        this.tables = tables;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TableCheckboxViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemTableCheckboxBinding binding = DataBindingUtil.inflate(inflater, R.layout.item_table_checkbox, parent, false);

        return new TableCheckboxViewHolder(binding, presenter);
    }

    @Override
    public void onBindViewHolder(@NonNull TableCheckboxViewHolder holder, int position) {
        holder.bind(tables.get(position));
    }

    @Override
    public int getItemCount() {
        return tables.size();
    }
}
