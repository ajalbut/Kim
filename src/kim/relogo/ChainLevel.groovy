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
	static RHO = 0.5
	static ALPHA = 0.5
	static BETA = 1.0
	static THETA = 0.5
	static String RULE = 'TRUST'

	def desiredStock = 20.0
	def currentStock
	def expectedDemand

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
	Map ordersSentChecklist = [:]
	Map shipmentsReceivedChecklist = [:]
	Map shipmentsSentChecklist = [:]
	Map ordersReceivedChecklist = [:]
	Map trustUpstreams = [:]
	Map trustDownstreams = [:]

	def upstreamLevel
	def downstreamLevel

	def initialProductPipeline = []
	def initialOrderPipeline = [4.0]
	def initialOrdersSentChecklist = [4.0, 4.0, 4.0, 4.0, 4.0]
	def initialShipmentsReceivedChecklist = [4.0, 4.0, 4.0]
	def initialShipmentsSentChecklist = [4.0, 4.0, 4.0, 4.0, 4.0]
	def initialOrdersReceivedChecklist = [4.0, 4.0]

	def setup(x, y, initialStock){
		setxy(x,y)
		setShape("square")
		setColor(white())
		this.currentStock = initialStock
		this.expectedDemand = 12.0
		for (ChainLevel upstream in this.upstreamLevel) {
			this.lastOrdersSent[upstream.getWho()] = 4.0
			this.lastProductPipelines[upstream.getWho()] = initialProductPipeline.clone()
			if(upstream.getWho() != this.getWho()) {
				this.ordersSentChecklist[upstream.getWho()] = initialOrdersSentChecklist.clone()
				this.shipmentsReceivedChecklist[upstream.getWho()] = initialShipmentsReceivedChecklist.clone()
				this.trustUpstreams[upstream.getWho()] = 1.0
			}
		}
		for (ChainLevel downstream in this.downstreamLevel) {
			this.lastOrdersToFulfill[downstream.getWho()] = 0.0
			this.lastShipmentsSent[downstream.getWho()] = 4.0
			this.lastOrderPipelines[downstream.getWho()] = initialOrderPipeline.clone()
			this.shipmentsSentChecklist[downstream.getWho()] = initialShipmentsSentChecklist.clone()
			this.ordersReceivedChecklist[downstream.getWho()] = initialOrdersReceivedChecklist.clone()
			this.trustDownstreams[downstream.getWho()] = 1.0
		}
	}

	def receiveShipments(){
		for (ChainLevel upstream in this.upstreamLevel) {
			this.productPipelines[upstream.getWho()] = this.lastProductPipelines[upstream.getWho()].clone()
			this.productPipelines[upstream.getWho()].add(0, upstream.lastShipmentsSent[this.getWho()])
			def shipmentReceived = this.productPipelines[upstream.getWho()].pop()
			this.currentStock += shipmentReceived
			if(upstream.getWho() != this.getWho()) {
				this.shipmentsReceivedChecklist[upstream.getWho()].add(0, shipmentReceived)
			}
		}
	}

	def receiveOrders(){
		for (ChainLevel downstream in this.downstreamLevel) {
			this.orderPipelines[downstream.getWho()] = this.lastOrderPipelines[downstream.getWho()].clone()
			this.orderPipelines[downstream.getWho()].add(0, downstream.lastOrdersSent[this.getWho()])
			def orderReceived = this.orderPipelines[downstream.getWho()].pop()
			this.ordersToFulfill[downstream.getWho()] = this.lastOrdersToFulfill[downstream.getWho()] + orderReceived
			this.ordersReceived[downstream.getWho()] = orderReceived
			this.ordersReceivedChecklist[downstream.getWho()].add(0, this.ordersReceived[downstream.getWho()])
		}
	}

	def fulfillOrders(){
		def totalOrdersToFulfill = 0.0
		def totalTrustDownstreams = 0.0
		for (ChainLevel downstream in this.downstreamLevel) {
			totalOrdersToFulfill += this.ordersToFulfill[downstream.getWho()]
			totalTrustDownstreams += this.trustDownstreams[downstream.getWho()]
		}
		def totalShipmentsSent = (this.currentStock >= totalOrdersToFulfill) ? totalOrdersToFulfill : this.currentStock
		for (ChainLevel downstream in this.downstreamLevel) {
			def shipmentSent
			if (this.RULE == 'BACKORDER') {
				shipmentSent = totalOrdersToFulfill ? totalShipmentsSent * this.ordersToFulfill[downstream.getWho()] / totalOrdersToFulfill : 0.0
			} else {
				shipmentSent = totalTrustDownstreams ? totalShipmentsSent * this.trustDownstreams[downstream.getWho()] / totalTrustDownstreams : 0.0
			}
			this.currentStock -= shipmentSent
			this.ordersToFulfill[downstream.getWho()] -= shipmentSent
			this.shipmentsSent[downstream.getWho()] = shipmentSent
			this.shipmentsSentChecklist[downstream.getWho()].add(0, shipmentSent)
		}
	}

	def makeOrders(){
		def freeUpstreams = []
		def supplyLine = 0.0
		def inverseBackOrderSum = 0.0
		def totalTrustUpstreams = 0.0
		for (ChainLevel upstream in this.upstreamLevel) {
			supplyLine += this.productPipelines[upstream.getWho()].sum()
			if(upstream.getWho() != this.getWho()) {
				supplyLine += upstream.lastOrdersToFulfill[this.getWho()]
				supplyLine += upstream.lastOrderPipelines[this.getWho()].sum()
				if (this.RULE == 'BACKORDER') {
					if (!upstream.lastOrdersToFulfill[this.getWho()]) {
						freeUpstreams.push(upstream.getWho())
					} else {
						inverseBackOrderSum += 1 / upstream.lastOrdersToFulfill[this.getWho()]
					}
				} else {
					totalTrustUpstreams += this.trustUpstreams[upstream.getWho()]
				}
			}
		}

		def totalOrdersReceived = 0.0
		for (ChainLevel downstream in this.downstreamLevel) {
			totalOrdersReceived += this.ordersReceived[downstream.getWho()]
		}

		this.expectedDemand = this.THETA * totalOrdersReceived + (1 - this.THETA) * this.expectedDemand
		def desiredSupplyLine = 3 * this.expectedDemand
		def Q = this.desiredStock + this.BETA * desiredSupplyLine
		def totalOrders = this.expectedDemand + this.ALPHA * (Q - this.currentStock - this.BETA * supplyLine)
		def totalOrdersSent =  Math.max(0.0, totalOrders)

		for (ChainLevel upstream in this.upstreamLevel) {
			def orderSent
			if(upstream.getWho() == this.getWho()) {
				orderSent = totalOrdersSent
			} else {
				if (this.RULE == 'BACKORDER') {
					if(length(freeUpstreams)) {
						orderSent = (freeUpstreams.contains(upstream.getWho())) ? totalOrdersSent / length(freeUpstreams) : 0.0
					} else {
						orderSent = totalOrdersSent * (1 / upstream.lastOrdersToFulfill[this.getWho()]) / inverseBackOrderSum
					}
				} else {
					orderSent = totalTrustUpstreams ? totalOrdersSent * this.trustUpstreams[upstream.getWho()] / totalTrustUpstreams : 0.0
				}
				this.ordersSentChecklist[upstream.getWho()].add(0, orderSent)
			}
			this.ordersSent[upstream.getWho()] = orderSent
		}
	}

	def updateTrust(){
		for (ChainLevel upstream in this.upstreamLevel) {
			if(upstream.getWho() != this.getWho()) {
				def orderToCheck = this.ordersSentChecklist[upstream.getWho()].pop()
				def shipmentReceived = this.shipmentsReceivedChecklist[upstream.getWho()][3]
				def newEvaluation
				if (shipmentReceived >= orderToCheck) {
					newEvaluation = this.RHO * orderToCheck + (1 - this.RHO) * this.trustUpstreams[upstream.getWho()]
					this.trustUpstreams[upstream.getWho()] = Math.max(newEvaluation, this.trustUpstreams[upstream.getWho()])
				} else {
					newEvaluation = this.RHO * shipmentReceived + (1 - this.RHO) * this.trustUpstreams[upstream.getWho()]
					this.trustUpstreams[upstream.getWho()] = Math.min(newEvaluation, this.trustUpstreams[upstream.getWho()])
				}
			}
		}
		for (ChainLevel downstream in this.downstreamLevel) {
			def shipmentToCheck = this.shipmentsSentChecklist[downstream.getWho()].pop()
			def orderReceived = this.ordersReceivedChecklist[downstream.getWho()][2]
			def newEvaluation
			if (orderReceived > shipmentToCheck) {
				newEvaluation = this.RHO * shipmentToCheck + (1 - this.RHO) * this.trustDownstreams[downstream.getWho()]
				this.trustDownstreams[downstream.getWho()] = Math.max(newEvaluation, this.trustDownstreams[downstream.getWho()])
			} else {
				newEvaluation = this.RHO * orderReceived + (1 - this.RHO) * this.trustDownstreams[downstream.getWho()]
				this.trustDownstreams[downstream.getWho()] = Math.min(newEvaluation, this.trustDownstreams[downstream.getWho()])
			}
		}
	}

	def updateState(){
		for (ChainLevel upstream in this.upstreamLevel) {
			if(upstream.getWho() != this.getWho()) {
				this.shipmentsReceivedChecklist[upstream.getWho()].pop()
			}
			this.lastOrdersSent[upstream.getWho()] = this.ordersSent[upstream.getWho()]
			this.lastProductPipelines[upstream.getWho()] = this.productPipelines[upstream.getWho()].clone()
		}
		for (ChainLevel downstream in this.downstreamLevel) {
			this.ordersReceivedChecklist[downstream.getWho()].pop()
			this.lastOrdersToFulfill[downstream.getWho()] = this.ordersToFulfill[downstream.getWho()]
			this.lastShipmentsSent[downstream.getWho()] = this.shipmentsSent[downstream.getWho()]
			this.lastOrderPipelines[downstream.getWho()] = this.orderPipelines[downstream.getWho()].clone()
		}

		label = "" + this.currentStock
	}

	def getCurrentTrustFromUpstreams() {
		def trust = 0.0
		for (ChainLevel upstream in this.upstreamLevel) {
			trust += upstream.trustDownstreams[this.getWho()]
		}
		return trust
	}

	def getCurrentTrustFromDownstreams() {
		def trust = 0.0
		for (ChainLevel downstream in this.downstreamLevel) {
			trust += downstream.trustUpstreams[this.getWho()]
		}
		return trust
	}
}
