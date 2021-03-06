package protocolsupportpocketstuff.api.modals.elements.complex;

import java.util.ArrayList;
import java.util.List;

public class ModalStepSlider extends ModalComplexUIElement {
	
	private List<String> steps = new ArrayList<String>();
	private int defaultStepIndex;

	public ModalStepSlider(String text) {
		super(ComplexElementType.STEP_SLIDER);
		super.setText(text);
	}

	public ModalStepSlider setText(String text) {
		super.setText(text);
		return this;
	}

	public ModalStepSlider addStep(String optionText) {
		return addStep(optionText, false);
	}

	public ModalStepSlider addStep(String optionText, boolean isDefault) {
		if (isDefault) {
			defaultStepIndex = steps.size();
		}
		steps.add(optionText);
		return this;
	}

	public ModalStepSlider setDefaultStepIndex(int defaultStepIndex) {
		this.defaultStepIndex = defaultStepIndex;
		return this;
	}
	
	public int getDefaultStepIndex() {
		return defaultStepIndex;
	}

	public List<String> getOptions() {
		return steps;
	}

	public ModalStepSlider setSteps(List<String> steps) {
		this.steps = steps;
		return this;
	}
	
}
