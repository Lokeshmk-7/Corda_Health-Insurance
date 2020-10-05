package com.template.states

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
data class BeneficiaryDetail(val Name: String, val Age: Int, val Gender: Char)