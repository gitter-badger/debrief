package com.planetmayo.debrief.satc_rcp.ui.contributions;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;

import com.planetmayo.debrief.satc.model.contributions.BearingMeasurementContribution;
import com.planetmayo.debrief.satc.model.contributions.FrequencyMeasurementContribution;
import com.planetmayo.debrief.satc.model.generator.IContributions;
import com.planetmayo.debrief.satc_rcp.ui.UIUtils;
import com.planetmayo.debrief.satc_rcp.ui.converters.BooleanToNullConverter;
import com.planetmayo.debrief.satc_rcp.ui.converters.PrefixSuffixLabelConverter;

public class FrequencyMeasurementContributionView extends BaseContributionView<FrequencyMeasurementContribution>
{
	private Scale errorSlider;
	private Label errorLabel;
	private Button errorActiveCheckbox;	

	public FrequencyMeasurementContributionView(Composite parent,
			FrequencyMeasurementContribution contribution, IContributions contributions)
	{
		super(parent, contribution, contributions);
		initUI();
	}

	@Override
	protected void bindValues(DataBindingContext context)
	{
		PrefixSuffixLabelConverter labelConverter = new PrefixSuffixLabelConverter(Object.class, "+/- ", " Hz");
		IObservableValue errorValue = BeansObservables.observeValue(
				contribution, FrequencyMeasurementContribution.FREQUENCY_ERROR);
		IObservableValue estimateValue = BeansObservables.observeValue(
				contribution, BearingMeasurementContribution.ESTIMATE);		
		bindCommonHeaderWidgets(context, errorValue, estimateValue, 
				new PrefixSuffixLabelConverter(Object.class, " Measurements"), labelConverter);
		bindCommonDates(context);

		bindSliderLabelCheckbox(context, errorValue, errorSlider, errorLabel, errorActiveCheckbox,
				labelConverter, new BooleanToNullConverter<Double>(0d), null);		
	}
	
	@Override
	protected void createLimitAndEstimateSliders()
	{
		UIUtils.createLabel(bodyGroup, "Error: ", new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		
		Composite group = new Composite(bodyGroup, SWT.NONE);
		group.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		group.setLayout(UIUtils.createGridLayoutWithoutMargins(2, false));
		errorActiveCheckbox = new Button(group, SWT.CHECK);
		errorLabel = UIUtils.createSpacer(group, new GridData(GridData.FILL_HORIZONTAL));
		
		errorSlider = new Scale(bodyGroup, SWT.HORIZONTAL);
		errorSlider.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}
	
	@Override
	protected void initializeWidgets()
	{
		startDate.setEnabled(false);
		startTime.setEnabled(false);
		endDate.setEnabled(false);
		endTime.setEnabled(false);
	}

	@Override
	protected String getTitlePrefix()
	{
		return "Frequency Measurement - ";
	}	
}