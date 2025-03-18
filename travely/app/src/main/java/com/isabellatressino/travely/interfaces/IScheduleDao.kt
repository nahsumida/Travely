package com.isabellatressino.travely.interfaces

import com.isabellatressino.travely.models.User

interface IScheduleDao {

    fun registerCachaca(user: User, callback: (Boolean) -> Unit)

}