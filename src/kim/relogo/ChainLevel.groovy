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
	float desiredSupplyLine = 60
	float Q
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

	def upstreamLevel
	def downstreamLevel

	def initialProductPipeline = []
	def initialOrderPipeline = [4.0]

	def setup(initialStock){
		this.Q = this.desiredStock + this.BETA * this.desiredSupplyLine
		this.currentStock = initialStock
		this.expectedDemand = 12.0
		for (ChainLevel upstream in this.upstreamLevel) {
			this.lastOrdersSent[upstream.getWho()] = 4.0
			this.lastProductPipelines[upstream.getWho()] = initialProductPipeline
		}
		for (ChainLevel downstream in this.downstreamLevel) {
			this.lastOrdersToFulfill[downstream.getWho()] = 0.0
			this.lastShipmentsSent[downstream.getWho()] = 4.0
			this.lastOrderPipelines[downstream.getWho()] = initialOrderPipeline
		}
	}

	def receiveShipments(){
		this.productPipelines = this.lastProductPipelines
		for (ChainLevel upstream in this.upstreamLevel) {
			printf 'pipelineBefore' + this.productPipelines[upstream.getWho()] + '\n'
			printf 'addedShipment' + upstream.lastShipmentsSent[this.getWho()] + '\n'
			this.productPipelines[upstream.getWho()].add(0, upstream.lastShipmentsSent[this.getWho()])
			printf 'pipelineMid' + this.productPipelines[upstream.getWho()] + '\n'
			this.currentStock += this.productPipelines[upstream.getWho()].pop()
			printf 'pipelineAfter' + this.productPipelines[upstream.getWho()] + '\n'
		}
	}

	def receiveOrders(){
		this.orderPipelines = this.lastOrderPipelines 
		for (ChainLevel downstream in this.downstreamLevel) {
			this.orderPipelines[downstream.getWho()].add(0, downstream.lastOrdersSent[this.getWho()])
			this.ordersToFulfill[downstream.getWho()] = this.lastOrdersToFulfill[downstream.getWho()] + this.orderPipelines[downstream.getWho()].pop()
		}
	}

	def fulfillOrders(){
		float totalOrdersToFulfill = 0.0
		for (ChainLevel downstream in this.downstreamLevel) {
			totalOrdersToFulfill += this.ordersToFulfill[downstream.getWho()]
		}
		float totalShipmentsSent = (this.currentStock >= totalOrdersToFulfill) ? totalOrdersToFulfill : this.currentStock 
		for (ChainLevel downstream in this.downstreamLevel) {
//			printf 'ordersToFulfillWho '+ this.ordersToFulfill[downstream.getWho()] + '-' + downstream.getWho() + '\n'
//			printf 'currentStockWho '+ this.currentStock + '-' + this.getWho() + '\n'
//			printf 'totalOrdersToFulfill '+ totalOrdersToFulfill + '\n'
			this.shipmentsSent[downstream.getWho()] = totalOrdersToFulfill ? totalShipmentsSent * this.ordersToFulfill[downstream.getWho()] / totalOrdersToFulfill : 0.0
//			printf 'shipmentsSentWho '+ this.shipmentsSent[downstream.getWho()] + '-' + downstream.getWho() + '\n'
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
				printf 'upstreamOrdersThis' + upstream.lastOrdersToFulfill[this.getWho()]
				if (!upstream.lastOrdersToFulfill[this.getWho()]) {
					freeUpstreams.push(upstream.getWho())
				} else {
					inverseBackOrderSum += 1 / upstream.lastOrdersToFulfill[this.getWho()]
				}
			}
		}
		
		float totalShipmentsSent = 0.0
		for (ChainLevel downstream in this.downstreamLevel) {
			totalShipmentsSent += this.shipmentsSent[downstream.getWho()]
		}
		
		this.expectedDemand = this.THETA * totalShipmentsSent + (1 - this.THETA) * this.expectedDemand
		float totalOrders = this.expectedDemand + this.ALPHA * (this.Q - this.currentStock - this.BETA * supplyLine)
		float totalOrdersSent =  Math.max(0, totalOrders)
		
		for (ChainLevel upstream in this.upstreamLevel) {
			if(upstream.getWho() == this.getWho()) {
				this.ordersSent[upstream.getWho()] = totalOrdersSent
			} else {
				if(length(freeUpstreams)) {
//					printf 'supplyLine' + supplyLine + '\n'
//					printf 'totalOrders' + totalOrders + '\n'
//					printf 'totalOrdersSent' + totalOrdersSent + '\n'
//					printf 'upstreamWho' + upstream.getWho() + '\n'
//					printf 'freeUpstreams '+ freeUpstreams + '\n'
//					printf 'testcontains '+ freeUpstreams.contains(upstream.getWho()) + '\n'
					this.ordersSent[upstream.getWho()] = (freeUpstreams.contains(upstream.getWho())) ? totalOrdersSent / length(freeUpstreams) : 0.0
//					printf 'ordersSentWho' + this.ordersSent[upstream.getWho()] + '\n'
				} else {
					this.ordersSent[upstream.getWho()] = totalOrdersSent * (1 / upstream.lastOrdersToFulfill[this.getWho()]) / inverseBackOrderSum
				}
			}
		}
	}

	def updateState(){
//		printf 'ordersToFulfill'+ this.ordersToFulfill + '\n'
//		printf 'ordersSent'+ this.ordersSent + '\n'
//		printf 'shipmentsSent'+ this.shipmentsSent + '\n'
//		printf 'productPipelines'+ this.productPipelines + '\n'
//		printf 'orderPipelines'+ this.orderPipelines + '\n\n'
		this.lastOrdersToFulfill = this.ordersToFulfill
		this.lastOrdersSent = this.ordersSent
		this.lastShipmentsSent = this.shipmentsSent
		this.lastProductPipelines = this.productPipelines
		this.lastOrderPipelines = this.orderPipelines
	}
}
