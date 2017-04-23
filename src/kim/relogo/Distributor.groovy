package kim.relogo

import static repast.simphony.relogo.Utility.*
import static repast.simphony.relogo.UtilityG.*

import kim.ReLogoTurtle
import repast.simphony.relogo.Plural
import repast.simphony.relogo.Stop
import repast.simphony.relogo.Utility
import repast.simphony.relogo.UtilityG
import repast.simphony.relogo.schedule.Go
import repast.simphony.relogo.schedule.Setup

class Distributor extends ChainLevel {
	def setup(initialStock){
		this.initialProductPipeline = [4.0, 4.0]
		this.upstreamLevel = factories()
		this.downstreamLevel = wholesalers()
		super.setup(initialStock)
	}
}
