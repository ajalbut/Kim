package kim.relogo

import static repast.simphony.relogo.Utility.*;
import static repast.simphony.relogo.UtilityG.*;
import repast.simphony.relogo.Stop;
import repast.simphony.relogo.Utility;
import repast.simphony.relogo.UtilityG;
import repast.simphony.relogo.schedule.Go;
import repast.simphony.relogo.schedule.Setup;
import kim.ReLogoObserver;

class UserObserver extends ReLogoObserver{

	@Setup
	def setup(){
		clearAll()

		createFactories(3)
		createDistributors(3)
		createWholesalers(3)
		createRetailers(3)
		createCustomers(3)

		def factories = factories()
		def distributors = distributors()
		def wholesalers = wholesalers()
		def retailers = retailers()
		ask(factories[0]){ setup(40.0) }
		ask(factories[1]){ setup(20.0) }
		ask(factories[2]){ setup(0.0) }
		ask(distributors[0]){ setup(40.0) }
		ask(distributors[1]){ setup(20.0) }
		ask(distributors[2]){ setup(0.0) }
		ask(wholesalers[0]){ setup(40.0) }
		ask(wholesalers[1]){ setup(20.0) }
		ask(wholesalers[2]){ setup(0.0) }
		ask(retailers[0]){ setup(40.0) }
		ask(retailers[1]){ setup(20.0) }
		ask(retailers[2]){ setup(0.0) }
		ask(customers()) {setup(0.0)}
		
	}

	@Go
	def go(){
		tick()
		ask(chainLevels()){
			receiveShipments()
			receiveOrders()
			fulfillOrders()
			makeOrders()
		}
		ask(chainLevels()){ updateState() }
	}

	def getFactory0Stock(){
		return factories()[0].currentStock
	}

	def getDistributor0Stock(){
		return distributors()[0].currentStock
	}

	def getWholesaler0Stock(){
		return wholesalers()[0].currentStock
	}

	def getRetailer0Stock(){
		return retailers()[0].currentStock
	}

	def getFactory1Stock(){
		return factories()[1].currentStock
	}

	def getDistributor1Stock(){
		return distributors()[1].currentStock
	}

	def getWholesaler1Stock(){
		return wholesalers()[1].currentStock
	}

	def getRetailer1Stock(){
		return retailers()[1].currentStock
	}

	def getFactory2Stock(){
		return factories()[2].currentStock
	}

	def getDistributor2Stock(){
		return distributors()[2].currentStock
	}

	def getWholesaler2Stock(){
		return wholesalers()[2].currentStock
	}

	def getRetailer2Stock(){
		return retailers()[2].currentStock
	}
}