package org.dhis2.usescases.datasets.dataSetTable.dataSetSection;

import android.content.Context;

import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.dhis2.App;

import com.evrencoskun.tableview.TableView;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.collect.Table;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.tablefields.FieldViewModel;
import org.dhis2.data.forms.dataentry.tablefields.FieldViewModelFactoryImpl;
import org.dhis2.data.forms.dataentry.tablefields.RowAction;
import org.dhis2.databinding.FragmentDatasetSectionBinding;
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableActivity;
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableContract;
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableModel;
import org.dhis2.usescases.general.FragmentGlobalAbstract;
import org.dhis2.utils.ColorUtils;
import org.dhis2.utils.Constants;
import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.category.CategoryOptionModel;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.dataelement.DataElementModel;
import org.hisp.dhis.android.core.dataset.DataInputPeriodModel;
import org.hisp.dhis.android.core.dataset.DataSetModel;
import org.hisp.dhis.android.core.dataset.SectionModel;
import org.hisp.dhis.android.core.period.PeriodModel;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;

import io.reactivex.Flowable;
import timber.log.Timber;

/**
 * QUADRAM. Created by ppajuelo on 02/10/2018.
 */

public class DataSetSectionFragment extends FragmentGlobalAbstract implements DataValueContract.View {

    FragmentDatasetSectionBinding binding;
    private DataSetTableContract.Presenter presenter;
    private DataSetTableActivity activity;
    private DataSetTableAdapter adapter;
    private String section;
    private boolean accessDataWrite;
    private boolean tableCreated = false;
    private String dataSetUid;

    private PeriodModel periodModel;
    private DataInputPeriodModel dataInputPeriodModel;
    @Inject
    DataValueContract.Presenter presenterFragment;

    private TableView tableView;
    @NonNull
    public static DataSetSectionFragment create(@NonNull String sectionUid, boolean accessDataWrite, String dataSetUid) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.DATA_SET_SECTION, sectionUid);
        bundle.putBoolean(Constants.ACCESS_DATA, accessDataWrite);
        DataSetSectionFragment dataSetSectionFragment = new DataSetSectionFragment();
        dataSetSectionFragment.setArguments(bundle);
        bundle.putString(Constants.DATA_SET_UID, dataSetUid);
        return dataSetSectionFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (DataSetTableActivity) context;
        presenter = ((DataSetTableActivity) context).getPresenter();
        dataSetUid = getArguments().getString(Constants.DATA_SET_UID, dataSetUid);
        ((App) context.getApplicationContext()).userComponent().plus(new DataValueModule(dataSetUid)).inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_dataset_section, container, false);

        /*adapter = new DataSetTableAdapter(getAbstracContext());

        tableView = new TableView(getContext());
        tableView.setBackgroundColor(getResources().getColor(R.color.white));
        tableView.setUnSelectedColor(getResources().getColor(R.color.table_bg));
        tableView.setSelectedColor(getResources().getColor(R.color.colorPrimaryLight));
        tableView.setShadowColor(getResources().getColor(R.color.rfab__color_shadow));
        tableView.setRowHeaderWidth(350);
        binding.tableLayout.addView(tableView);
        *//*binding.tableView.setAdapter(adapter);
        binding.tableView.setEnabled(false);*//*
        tableView.setAdapter(adapter);*/
        binding.setPresenter(presenterFragment);
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        section = getArguments().getString(Constants.DATA_SET_SECTION);
        accessDataWrite = getArguments().getBoolean(Constants.ACCESS_DATA);
        presenterFragment.init(this, presenter.getOrgUnitUid(), presenter.getPeriodTypeName(),
                presenter.getPeriodFinalDate(), presenter.getCatCombo(), section, presenter.getPeriodId());
        presenterFragment.getData(this, section);
        /*presenterFragment.initializeProcessor(this);*/
    }


    public void createTable(DataTableModel dataTableModel) {
        DataSetModel dataSet = dataTableModel.dataSet();
        boolean isEditable = false;
        if(dataSet.accessDataWrite() &&
                !isExpired(dataTableModel.dataSet()) &&
                (dataInputPeriodModel == null || DateUtils.getInstance().isInsideInputPeriod(dataInputPeriodModel)) ){
            isEditable = true;
        }

        presenterFragment.setCurrentNumTables(dataTableModel.catCombos().size());
        activity.updateTabLayout(section, dataTableModel.catCombos().size());
        adapter = new DataSetTableAdapter(getAbstracContext() , presenterFragment.getProcessor());
        presenterFragment.initializeProcessor(this);
        for(String catCombo: dataTableModel.catCombos()) {
            List<List<CategoryOptionModel>> columnHeaderItems = dataTableModel.headers().get(catCombo);
            ArrayList<List<String>> cells = new ArrayList<>();
            List<List<FieldViewModel>> listFields = new ArrayList<>();
            List<DataElementModel> rows = new ArrayList<>();
            List<List<String>> listCatOptions = presenterFragment.getCatOptionCombos(dataTableModel.listCatOptionsCatComboOptions().get(catCombo), 0, new ArrayList<>(), null);
            int countColumn = 0;
            boolean isNumber = false;
            int row = 0, column = 0;
            adapter.setShowColumnTotal(dataTableModel.section() == null? false :dataTableModel.section().showColumnTotals());
            adapter.setShowRowTotal(dataTableModel.section() == null? false :dataTableModel.section().showRowTotals());
            adapter.initializeRows(isEditable);
            TableView tableView = new TableView(getContext());
            tableView.setUnSelectedColor(getResources().getColor(R.color.white));
            tableView.setHeadersColor(getResources().getColor(R.color.table_bg));
            tableView.setSelectedColor(ColorUtils.getPrimaryColor(getContext(), ColorUtils.ColorType.PRIMARY_LIGHT));
            tableView.setShadowColor(getResources().getColor(R.color.rfab__color_shadow));
            tableView.setRowHeaderWidth(350);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            layoutParams.setMargins(0, 0, 0, 40);


            binding.tableLayout.addView(tableView, layoutParams);
            /*binding.tableView.setAdapter(adapter);
            binding.tableView.setEnabled(false);*/
            tableView.setAdapter(adapter);
            tableView.setHeaderCount(columnHeaderItems.size());
            for (DataElementModel de : dataTableModel.rows()) {
                if(de.categoryCombo().equals(catCombo))
                    rows.add(de);

                ArrayList<String> values = new ArrayList<>();
                ArrayList<FieldViewModel> fields = new ArrayList<>();
                int totalRow = 0;
                if(de.categoryCombo().equals(catCombo)) {
                    for (List<String> catOpts : listCatOptions) {
                        boolean exitsValue = false;
                        boolean compulsory = false;
                        FieldViewModelFactoryImpl fieldFactory = createField();

                        boolean editable = /*!dataTableModel.dataElementDisabled().containsKey(section) || */!dataTableModel.dataElementDisabled().containsKey(de.uid())
                                || !dataTableModel.dataElementDisabled().get(de.uid()).containsAll(catOpts);

                        if (dataTableModel.compulsoryCells().containsKey(de.uid()) && dataTableModel.compulsoryCells().get(de.uid()).containsAll(catOpts))
                            compulsory = true;

                        if (de.valueType() == ValueType.NUMBER || de.valueType() == ValueType.INTEGER) {
                            isNumber = true;
                        }

                        for (DataSetTableModel dataValue : dataTableModel.dataValues()) {

                            if (dataValue.listCategoryOption().containsAll(catOpts)
                                    && Objects.equals(dataValue.dataElement(), de.uid()) && dataValue.catCombo().equals(catCombo)) {

                                if (isNumber) {
                                    if (adapter.getShowRowTotal())
                                        totalRow = totalRow + Integer.parseInt(dataValue.value());
                                }

                                fields.add(fieldFactory.create(dataValue.id().toString(), "", de.valueType(),
                                        compulsory, "", dataValue.value(), section, true,
                                        editable, null, null, de.uid(), catOpts, "", row, column, dataValue.categoryOptionCombo(), dataValue.catCombo()));
                                values.add(dataValue.value());
                                exitsValue = true;
                            }
                        }

                        if (!exitsValue) {
                            //If value type is null, it is due to is dataElement for Total row/column
                            fields.add(fieldFactory.create("", "", de.valueType(),
                                    compulsory, "", "", section, true,
                                    editable, null, null, de.uid() == null ? "" : de.uid(), catOpts, "", row, column, ""/*SET CATEGORYOPTIONCOMBO*/, ""));

                            values.add("");
                        }
                        countColumn++;
                        column++;
                    }
                    countColumn = 0;
                    if (isNumber && adapter.getShowRowTotal()) {
                        setTotalRow(totalRow, fields, values, row, column);
                    }
                    listFields.add(fields);
                    cells.add(values);
                    column = 0;
                    row++;
                }
            }

            if (isNumber) {
                if (adapter.getShowColumnTotal())
                    setTotalColumn(listFields, cells, rows, row, column);
                if (adapter.getShowRowTotal())
                    for (int i = 0; i < columnHeaderItems.size(); i++) {
                        if (i == columnHeaderItems.size() - 1)
                            columnHeaderItems.get(i).add(CategoryOptionModel.builder().displayName(getString(R.string.total)).build());
                        else
                            columnHeaderItems.get(i).add(CategoryOptionModel.builder().displayName("").build());
                    }

            }

            adapter.swap(listFields);
            //if (!tableCreated)
                adapter.setAllItems(
                        dataTableModel.headers().get(catCombo),
                        rows,
                        cells, adapter.getShowRowTotal());
            /*else
                adapter.setCellItems(cells);

            tableCreated = false;*/

            if(!catCombo.equals(dataTableModel.catCombos().get(dataTableModel.catCombos().size()-1)))
                adapter = new DataSetTableAdapter(getAbstracContext(), presenterFragment.getProcessor());

        }

        binding.scroll.scrollTo(0,1000);
    }

    private void setTotalColumn(List<List<FieldViewModel>> listFields, ArrayList<List<String>> cells,
                                List<DataElementModel> dataElements, int row, int columnPos) {
        FieldViewModelFactoryImpl fieldFactory = createField();

        ArrayList<FieldViewModel> fields = new ArrayList<>();
        ArrayList<String> values = new ArrayList<>();
        boolean existTotal = false;
        for (DataElementModel data : dataElements)
            if (data.displayName().equals(getContext().getString(R.string.total)))
                existTotal = true;

        if (existTotal){
            listFields.remove(listFields.size()-1);
            cells.remove(listFields.size()-1);
        }


        int[] totals = new int[cells.get(0).size()];
        for(List<String> dataValues : cells){
            for (int i=0; i< dataValues.size(); i++){
                if(!dataValues.get(i).isEmpty())
                    totals[i] += Integer.parseInt(dataValues.get(i));
            }
        }

        for (int column : totals) {
            fields.add(fieldFactory.create("", "", ValueType.INTEGER,
                    false, "", String.valueOf(column), section, true,
                    false, null, null, "",new ArrayList<>(),"", row, columnPos, "", ""));

            values.add(String.valueOf(column));
        }


        listFields.add(fields);
        cells.add(values);

        if(!existTotal)
            dataElements.add(DataElementModel.builder().displayName(getString(R.string.total)).valueType(ValueType.INTEGER).build());
    }

    private void setTotalRow(int totalRow, ArrayList<FieldViewModel> fields, ArrayList<String> values, int row, int column){
        FieldViewModelFactoryImpl fieldFactory = createField();
        fields.add(fieldFactory.create("", "", ValueType.INTEGER,
                false, "", String.valueOf(totalRow), section, true,
                false, null, null, "",new ArrayList<>(),"", row, column, "", ""));
        values.add(String.valueOf(totalRow));

    }

    private FieldViewModelFactoryImpl createField(){
        return new FieldViewModelFactoryImpl(
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "");
    }

    @NonNull
    public Flowable<RowAction> rowActions() {
        return adapter.asFlowable();
    }

    public void updateData(RowAction rowAction) {
        adapter.updateValue(rowAction);
    }

    @Override
    public void showSnackBar() {
        Snackbar mySnackbar = Snackbar.make(binding.getRoot(), R.string.datavalue_saved , Snackbar.LENGTH_SHORT);
        mySnackbar.show();
    }

    @Override
    public void onComplete() {
        activity.finish();
    }

    @Override
    public void setPeriod(PeriodModel periodModel) {
        this.periodModel = periodModel;
    }

    private Boolean isExpired(DataSetModel dataSet) {

        if(0 == dataSet.expiryDays()){
            return false;
        }
        if(presenter.getPeriodFinalDate() != null) {
            try {
                return DateUtils.getInstance().isDataSetExpired(dataSet.expiryDays(), DateUtils.databaseDateFormat().parse(presenter.getPeriodFinalDate()));
            } catch (ParseException e) {
                Timber.e(e);
            }
        }

        return DateUtils.getInstance().isDataSetExpired(dataSet.expiryDays(), periodModel.endDate());
    }

    @Override
    public void setDataInputPeriod(DataInputPeriodModel dataInputPeriod) {
        this.dataInputPeriodModel = dataInputPeriod;
    }

    @Override
    public void goToTable(int numTable) {
        int[] position = new int[2];
        binding.tableLayout.getChildAt(numTable).getLocationOnScreen(position);
        binding.scroll.scrollTo(0, position[1]);
    }

    public int currentNumTables(){
        return presenterFragment!=null ? presenterFragment.getCurrentNumTables() : 0;
    }
}