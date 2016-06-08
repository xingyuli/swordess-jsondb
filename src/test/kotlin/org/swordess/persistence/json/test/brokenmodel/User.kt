package org.swordess.persistence.json.test.brokenmodel

import org.swordess.persistence.Id
import org.swordess.persistence.json.JsonEntity

@JsonEntity(filename = "user")
data class User(@get:Id val id: Long? = null, var username: String? = null)