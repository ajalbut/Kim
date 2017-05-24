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
	def setup(x, y, initialStock){
		this.initialProductPipeline = [12.0, 12.0, 12.0]
		this.upstreamLevel = [this]
		this.downstreamLevel = distributors()
		super.setup(x, y, initialStock)
	}

	def receiveShipments(){
		this.productPipelines[this.getWho()] = this.lastProductPipelines[this.getWho()].clone()
		this.productPipelines[this.getWho()].add(0, this.lastOrdersSent[this.getWho()])
		this.currentStock += this.productPipelines[this.getWho()].pop()
	}
}
