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

@Plural ("Factories")
class Factory extends ChainLevel {
	def setup(initialStock){
		this.desiredSupplyLine = 12
		this.initialProductPipeline = [4.0, 4.0, 4.0]
		this.upstreamLevel = [this]
		this.downstreamLevel = distributors()
		super.setup(initialStock)
	}

	def receiveShipments(){
		this.productPipelines = this.lastProductPipelines
		this.productPipelines[this.getWho()].add(0, this.lastOrdersSent[this.getWho()])
		this.currentStock += this.productPipelines[this.getWho()].pop()
	}
}
