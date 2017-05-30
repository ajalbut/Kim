package kim.relogo

import repast.simphony.relogo.factories.AbstractReLogoGlobalsAndPanelFactory

public class UserGlobalsAndPanelFactory extends AbstractReLogoGlobalsAndPanelFactory{
	public void addGlobalsAndPanelComponents(){

		/**
		 * Place custom panels and globals below, for example:
		 * 
		 addGlobal("globalVariable1")	// Globally accessible variable ( variable name)
		 // Slider with label ( variable name, slider label, minimum value, increment, maximum value, initial value )
		 addSliderWL("sliderVariable", "Slider Variable", 0, 1, 10, 5)
		 // Slider without label ( variable name, minimum value, increment, maximum value, initial value )
		 addSlider("sliderVariable2", 0.2, 0.1, 0.8, 0.5)
		 // Chooser with label  ( variable name, chooser label, list of choices , zero-based index of initial value )
		 addChooserWL("chooserVariable", "Chooser Variable", ["yes","no","maybe"], 2)
		 // Chooser without label  ( variable name, list of choices , zero-based index of initial value )
		 addChooser("chooserVariable2", [1, 66, "seven"], 0)
		 // State change button (method name in observer)
		 addStateChangeButton("change")
		 // State change button with label (method name in observer, label)
		 addStateChangeButtonWL("changeSomething","Change Something")
		 */
		addChooserWL("supplyRule", "Supply Rule", ["BACKORDER","TRUST"], 0)
		addStateChangeButtonWL("toggleTrustVisibility","Toggle Trust visibility")
		
		addMonitor("getGlobalUtility", 1)
		
		addMonitor("getFactory0Stock", 1)
		addMonitor("getDistributor0Stock", 1)
		addMonitor("getWholesaler0Stock", 1)
		addMonitor("getRetailer0Stock", 1)
		addMonitor("getFactory1Stock", 1)
		addMonitor("getDistributor1Stock", 1)
		addMonitor("getWholesaler1Stock", 1)
		addMonitor("getRetailer1Stock", 1)
		addMonitor("getFactory2Stock", 1)
		addMonitor("getDistributor2Stock", 1)
		addMonitor("getWholesaler2Stock", 1)
		addMonitor("getRetailer2Stock", 1)
		
		addMonitor("getDistributor0TrustFromUpstreams", 1)
		addMonitor("getWholesaler0TrustFromUpstreams", 1)
		addMonitor("getRetailer0TrustFromUpstreams", 1)
		addMonitor("getCustomer0TrustFromUpstreams", 1)
		addMonitor("getDistributor1TrustFromUpstreams", 1)
		addMonitor("getWholesaler1TrustFromUpstreams", 1)
		addMonitor("getRetailer1TrustFromUpstreams", 1)
		addMonitor("getCustomer1TrustFromUpstreams", 1)
		addMonitor("getDistributor2TrustFromUpstreams", 1)
		addMonitor("getWholesaler2TrustFromUpstreams", 1)
		addMonitor("getRetailer2TrustFromUpstreams", 1)
		addMonitor("getCustomer2TrustFromUpstreams", 1)
		
		addMonitor("getFactory0TrustFromDownstreams", 1)
		addMonitor("getDistributor0TrustFromDownstreams", 1)
		addMonitor("getWholesaler0TrustFromDownstreams", 1)
		addMonitor("getRetailer0TrustFromDownstreams", 1)
		addMonitor("getFactory1TrustFromDownstreams", 1)
		addMonitor("getDistributor1TrustFromDownstreams", 1)
		addMonitor("getWholesaler1TrustFromDownstreams", 1)
		addMonitor("getRetailer1TrustFromDownstreams", 1)
		addMonitor("getFactory2TrustFromDownstreams", 1)
		addMonitor("getDistributor2TrustFromDownstreams", 1)
		addMonitor("getWholesaler2TrustFromDownstreams", 1)
		addMonitor("getRetailer2TrustFromDownstreams", 1)
	}
}