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

class ChainLevel extends ReLogoTurtle {
	static float RHO = 0.5
	static float ALPHA = 0.5
	static float BETA = 1.0
	static float THETA = 0.5

	float desiredStock = 20
	float currentStock
	float expectedDemand

	Map ordersToFulfill = [:]
	Map ordersSent = [:]
	Map shipmentsSent = [:]
	Map productPipelines = [:]
	Map orderPipelines = [:]
	Map lastOrdersToFulfill = [:]
	Map lastOrdersSent = [:]
	Map lastShipmentsSent = [:]
	Map lastProductPipelines = [:]
	Map lastOrderPipelines = [:]
	Map ordersReceived = [:]

	def upstreamLevel
	def downstreamLevel

	def initialProductPipeline = []
	def initialOrderPipeline = [4.0]

	def setup(initialStock){
		this.currentStock = initialStock
		this.expectedDemand = 12.0
		for (ChainLevel upstream in this.upstreamLevel) {
			this.lastOrdersSent[upstream.getWho()] = 4.0
			this.lastProductPipelines[upstream.getWho()] = initialProductPipeline.clone()
		}
		for (ChainLevel downstream in this.downstreamLevel) {
			this.lastOrdersToFulfill[downstream.getWho()] = 0.0
			this.lastShipmentsSent[downstream.getWho()] = 4.0
			this.lastOrderPipelines[downstream.getWho()] = initialOrderPipeline.clone()
		}
	}

	def receiveShipments(){
		for (ChainLevel upstream in this.upstreamLevel) {
			this.productPipelines[upstream.getWho()] = this.lastProductPipelines[upstream.getWho()].clone()
			this.productPipelines[upstream.getWho()].add(0, upstream.lastShipmentsSent[this.getWho()])
			this.currentStock += this.productPipelines[upstream.getWho()].pop()
		}
	}

	def receiveOrders(){
		for (ChainLevel downstream in this.downstreamLevel) {
			this.orderPipelines[downstream.getWho()] = this.lastOrderPipelines[downstream.getWho()].clone()
			this.orderPipelines[downstream.getWho()].add(0, downstream.lastOrdersSent[this.getWho()])
			this.ordersReceived[downstream.getWho()] = this.orderPipelines[downstream.getWho()].pop()
			this.ordersToFulfill[downstream.getWho()] = this.lastOrdersToFulfill[downstream.getWho()] + this.ordersReceived[downstream.getWho()]
		}
	}

	def fulfillOrders(){
		float totalOrdersToFulfill = 0.0
		for (ChainLevel downstream in this.downstreamLevel) {
			totalOrdersToFulfill += this.ordersToFulfill[downstream.getWho()]
		}
		float totalShipmentsSent = (this.currentStock >= totalOrdersToFulfill) ? totalOrdersToFulfill : this.currentStock
		for (ChainLevel downstream in this.downstreamLevel) {
			this.shipmentsSent[downstream.getWho()] = totalOrdersToFulfill ? totalShipmentsSent * this.ordersToFulfill[downstream.getWho()] / totalOrdersToFulfill : 0.0
			this.currentStock -= this.shipmentsSent[downstream.getWho()]
			this.ordersToFulfill[downstream.getWho()] -= this.shipmentsSent[downstream.getWho()]
		}
	}

	def makeOrders(){
		def freeUpstreams = []
		float supplyLine = 0.0
		float inverseBackOrderSum = 0.0
		for (ChainLevel upstream in this.upstreamLevel) {
			supplyLine += this.productPipelines[upstream.getWho()].sum()
			if(upstream.getWho() != this.getWho()) {
				supplyLine += upstream.lastOrdersToFulfill[this.getWho()]
				supplyLine += upstream.lastOrderPipelines[this.getWho()].sum()
				if (!upstream.lastOrdersToFulfill[this.getWho()]) {
					freeUpstreams.push(upstream.getWho())
				} else {
					inverseBackOrderSum += 1 / upstream.lastOrdersToFulfill[this.getWho()]
				}
			}
		}

		float totalOrdersReceived = 0.0
		for (ChainLevel downstream in this.downstreamLevel) {
			totalOrdersReceived += this.ordersReceived[downstream.getWho()]
		}

		this.expectedDemand = this.THETA * totalOrdersReceived + (1 - this.THETA) * this.expectedDemand
		float desiredSupplyLine = 3 * this.expectedDemand
		float Q = this.desiredStock + this.BETA * desiredSupplyLine
		float totalOrders = this.expectedDemand + this.ALPHA * (Q - this.currentStock - this.BETA * supplyLine)
		float totalOrdersSent =  Math.max(0, totalOrders)

		for (ChainLevel upstream in this.upstreamLevel) {
			if(upstream.getWho() == this.getWho()) {
				this.ordersSent[upstream.getWho()] = totalOrdersSent
			} else {
				if(length(freeUpstreams)) {
					this.ordersSent[upstream.getWho()] = (freeUpstreams.contains(upstream.getWho())) ? totalOrdersSent / length(freeUpstreams) : 0.0
				} else {
					this.ordersSent[upstream.getWho()] = totalOrdersSent * (1 / upstream.lastOrdersToFulfill[this.getWho()]) / inverseBackOrderSum
				}
			}
		}
	}

	def updateState(){
		for (ChainLevel downstream in this.downstreamLevel) {
			this.lastOrdersToFulfill[downstream.getWho()] = this.ordersToFulfill[downstream.getWho()]
			this.lastShipmentsSent[downstream.getWho()] = this.shipmentsSent[downstream.getWho()]
			this.lastOrderPipelines[downstream.getWho()] = this.orderPipelines[downstream.getWho()].clone()
		}
		for (ChainLevel upstream in this.upstreamLevel) {
			this.lastOrdersSent[upstream.getWho()] = this.ordersSent[upstream.getWho()]
			this.lastProductPipelines[upstream.getWho()] = this.productPipelines[upstream.getWho()].clone()
		}
	}
}
