package com.tmas.tmas

import android.app.Activity
import android.content.Context
import android.location.Location
import android.os.Handler
import androidx.lifecycle.LifecycleOwner
import com.gofa.speedlimit.GofaSpeedLimit
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.ArrayList

class MainUpdate() {

    companion object {
        var muteGofa: Boolean = false
    }

    fun setMuteGofa(boolean: Boolean){
        muteGofa = boolean
    }

    fun updateLocation(context: Context, location: Location){
        GofaSpeedLimit.getInstance(context).updateLocation(location)
    }

    fun initGofa(context: Context, owner : LifecycleOwner) {
        GofaSpeedLimit.getInstance(context)
            .setGofaKey("zC8P3ekiwhTG4G4iNNm7dhG9sDwznFvY", "ed0c9e80-b180-4855-9a20-d8ab7e718e75")
        // 10 km/h
        GofaSpeedLimit.getInstance(context).setGpsBounds(1)
        // 100 m
        GofaSpeedLimit.getInstance(context).setDistanceShowSearchTrafficSign(300)
        if (!GofaSpeedLimit.getInstance(context).maxSpeed.hasObservers()) {
            GofaSpeedLimit.getInstance(context).maxSpeed.observe(owner) {
                try {
                    android.util.Log.d("GofaSpeedLimit", "Speed limit: $it")
                    MainController.mIsNeedUpdateSpeedLimit.postValue(it)
                } catch (e: Exception) {
                    android.util.Log.d("GofaSpeedLimit", e.toString())
                }

            }
        }
        if (!GofaSpeedLimit.getInstance(context).error.hasObservers()) {
            GofaSpeedLimit.getInstance(context).error.observe(owner) {
                try {
                    android.util.Log.d("GofaSpeedLimit", "OnError: $it")
                } catch (e: Exception) {
                    android.util.Log.d("GofaSpeedLimit", e.toString())
                }
            }
        }
        if (!GofaSpeedLimit.getInstance(context).listNextBoards.hasObservers()) {
            GofaSpeedLimit.getInstance(context).listNextBoards.observe(owner) {
                try {
                    android.util.Log.d("GofaSpeedLimit", "listNextBoards: $it")
                    val array: ArrayList<Int> = ArrayList()
                    it.forEach { trafficPoint ->
                        trafficPoint.trafficSignId?.let { it1 -> array.add(it1) }
                    }
                    MainController.mIsNeedUpdateSignNext.postValue(
                        array
                    )
                } catch (e: Exception) {
                    android.util.Log.d("GofaSpeedLimit", e.toString())
                }
            }
        }
        if (!GofaSpeedLimit.getInstance(context).listOtherBoards.hasObservers()) {
            GofaSpeedLimit.getInstance(context).listOtherBoards.observe(owner) {
                android.util.Log.d("GofaSpeedLimit", "listOtherBoards: $it")
                try {
                    val array: ArrayList<Int> = ArrayList()
                    it.forEach { trafficPoint ->
                        trafficPoint.trafficSignId?.let { it1 -> array.add(it1) }
                    }
                    MainController.mIsNeedUpdateSignOther.postValue(
                        array
                    )
                } catch (e: Exception) {
                    android.util.Log.d("GofaSpeedLimit", e.toString())
                }

            }
        }
        if (!GofaSpeedLimit.getInstance(context).speed.hasObservers()) {
            GofaSpeedLimit.getInstance(context).speed.observe(owner) {
                try {
                    val newDistance = if (it <= 50) 300 else 300 + (it - 50) * 2
                    GofaSpeedLimit.getInstance(context).setDistanceShowSearchTrafficSign(newDistance)
                } catch (e: Exception) {
                    android.util.Log.d("GofaSpeedLimit", e.toString())
                }

            }
        }

    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun setupSignView(view: SignView, owner: LifecycleOwner){
        view.setup(owner, muteGofa,MainController.mIsNeedUpdateSpeedLimit,MainController.mIsNeedUpdateSignNext,MainController.mIsNeedUpdateSignOther)
    }

}