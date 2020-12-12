package com.x930073498.component.auto.plugin.register

data class ScanJarHarvest(val harvestList:MutableList<Harvest>)


data class Harvest(val className: String, val interfaceName: String?, val isInitClass: Boolean)