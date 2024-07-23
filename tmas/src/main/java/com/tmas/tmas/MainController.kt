package com.tmas.tmas

import android.location.Location
import androidx.lifecycle.MutableLiveData

object MainController {
    const val MAX_ITEMS_OF_GROUP_24 = 24
    const val MAX_ITEMS_OF_GROUP_25 = 25

    var mIsResetApp = SingleLiveEvent<Boolean>()
    var mIsNeedNaviButton = SingleLiveEvent<Boolean>()
    var mIsShowEditQuickApp = MutableLiveData(false)
    var mIsShowHomePage = MutableLiveData<Boolean>()
    var isHomeShowing = MutableLiveData(true)
    var mIsReloadQuickApp = MutableLiveData<Boolean>()
    var mIsNeedUpdateTheme = MutableLiveData(false)
    var mIsNeedUpdateGlass = SingleLiveEvent<Boolean>()
    var mIsNeedUpdateDarkMode = SingleLiveEvent<Boolean>()
    var mIsNeedUpdateSpeedDif = SingleLiveEvent<Boolean>()
    var mEnableSlideViewPager = MutableLiveData(true)
    var mIsPlayingStube = MutableLiveData<Boolean>()
    var mIsNeedUpdateSpeedLimit = MutableLiveData<Int>()
    var mIsNeedUpdateSignNext = MutableLiveData<ArrayList<Int>>()
    var mIsNeedUpdateSignOther = MutableLiveData<ArrayList<Int>>()
    var mIsNeedUpdateWallpaper = SingleLiveEvent<String>()
    var mIsNeedUpdateLocation = SingleLiveEvent<Location>()
    var mIsNeedUpdateAdvance = SingleLiveEvent<Int>()
    var mIsShowTracking = SingleLiveEvent<Boolean>()
    var mIsCheckGPS = SingleLiveEvent<Boolean>()
    var mIsNeedUpdateUI = SingleLiveEvent<Boolean>()
    var mIsNeedUpdatePip = SingleLiveEvent<String>()
}
