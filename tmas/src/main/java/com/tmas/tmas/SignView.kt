package com.tmas.tmas

import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.tmas.tmas.databinding.ItemSignBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class SignView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var binding: ItemSignBinding

    private var listSign: ArrayList<String> = ArrayList()
    private var listSignNext: ArrayList<String> = ArrayList()
    private var listSignOther: ArrayList<String> = ArrayList()
    private var checkReSize025 = false

    init {
        val view = inflate(context, R.layout.item_sign, this)
        binding = ItemSignBinding.bind(view)
    }

    fun setup(lifecycleOwner: LifecycleOwner, muteGofa: Boolean, speedLimitLiveData: MutableLiveData<Int>, signNextLiveData: MutableLiveData<ArrayList<Int>>, signOtherLiveData: MutableLiveData<ArrayList<Int>>) {
        binding.tvSpeedLimit.setColors(R.color.black, R.color.black)
        updateMuteButton(muteGofa)

        binding.buttonMute.setOnClickListener {
            toggleMute()
        }

        speedLimitLiveData.observe(lifecycleOwner, Observer {
            updateSpeedLimit(it)
        })

        signNextLiveData.observe(lifecycleOwner, Observer { list ->
            updateSignNext(list)
        })

        signOtherLiveData.observe(lifecycleOwner, Observer { list ->
            updateSignOther(list)
        })
    }

    private fun updateMuteButton(muteGofa: Boolean) {
        if (muteGofa) {
            binding.buttonMute.setBackgroundResource(R.drawable.ic_gofa_off_volume)
            binding.buttonMute.alpha = 1.0f
        } else {
            binding.buttonMute.setBackgroundResource(R.drawable.ic_gofa_on_volume)
            binding.buttonMute.alpha = 0.2f
        }
    }

    private fun toggleMute() {
        if (MainUpdate.muteGofa) {
            val fadeOut = ObjectAnimator.ofFloat(binding.buttonMute, "alpha", 1.0f, 0.2f)
            fadeOut.duration = 1000 // Thời gian hiệu ứng (1 giây)
            fadeOut.start()
            AppUtils.toast(context, "Đã bật âm thanh cảnh báo", false)
            binding.buttonMute.setBackgroundResource(R.drawable.ic_gofa_on_volume)
        } else {
            AppUtils.toast(context, "Đã tắt âm thanh cảnh báo", false)
            binding.buttonMute.setBackgroundResource(R.drawable.ic_gofa_off_volume)
            binding.buttonMute.alpha = 1.0f
        }
        MainUpdate.muteGofa = !MainUpdate.muteGofa
    }

    private fun updateSpeedLimit(speedLimit: Int) {
        Log.d("GofaSpeedLimit", "tvSpeedLimit: $speedLimit")
        if (AppUtils.isNetworkAvailable(context)) {
            binding.btnSign1.setBackgroundResource(R.drawable.sign_limit_speed)
            when (speedLimit) {
                1 -> binding.btnSign1.setBackgroundResource(R.drawable.sign_no)
                40 -> context?.let { if (binding.tvSpeedLimit.text != speedLimit.toString()) AppUtils.enqueueSound(it, R.raw.gofa_speed40) }
                50 -> context?.let { if (binding.tvSpeedLimit.text != speedLimit.toString() && binding.tvSpeedLimit.text != "") AppUtils.enqueueSound(it, R.raw.gofa_speed50) }
                60 -> context?.let { if (binding.tvSpeedLimit.text != speedLimit.toString()) AppUtils.enqueueSound(it, R.raw.gofa_speed60) }
                70 -> context?.let { if (binding.tvSpeedLimit.text != speedLimit.toString()) AppUtils.enqueueSound(it, R.raw.gofa_speed70) }
                80 -> context?.let { if (binding.tvSpeedLimit.text != speedLimit.toString()) AppUtils.enqueueSound(it, R.raw.gofa_speed80) }
                90 -> context?.let { if (binding.tvSpeedLimit.text != speedLimit.toString()) AppUtils.enqueueSound(it, R.raw.gofa_speed90) }
                100 -> context?.let { if (binding.tvSpeedLimit.text != speedLimit.toString()) AppUtils.enqueueSound(it, R.raw.gofa_speed100) }
                120 -> context?.let { if (binding.tvSpeedLimit.text != speedLimit.toString()) AppUtils.enqueueSound(it, R.raw.gofa_speed120) }
            }
            if (speedLimit != 1) binding.tvSpeedLimit.text = speedLimit.toString()
            else binding.tvSpeedLimit.text = ""
        } else {
            binding.btnSign1.setBackgroundResource(R.drawable.sign_no)
            binding.tvSpeedLimit.text = ""
        }
    }

    private fun updateSignNext(list: List<Int>) {
        Log.d("GofaSpeedLimit", "mIsNeedUpdateSignNext: $list")
        listSignNext.clear()
        list.forEach {
            listSignNext.add(it.toString())
        }
        checkListSign()
    }

    private fun updateSignOther(list: List<Int>) {
        Log.d("GofaSpeedLimit", "mIsNeedUpdateSignOther: $list")
        listSignOther.clear()
        list.forEach {
            listSignOther.add(it.toString())
        }
        checkListSign()
    }

    private fun checkListSign() {
        listSign.clear()
        listSign.addAll(listSignNext)
        listSign.addAll(listSignOther)
        if (listSign.size > 2) {
            if (!checkReSize025) resizeSign(0.25f)
            updateSign(binding.btnSign3, listSign[0])
            updateSign(binding.btnSign4, listSign[1])
            updateSign(binding.btnSign5, listSign[2])
        } else {
            if (checkReSize025) resizeSign(0.3f)
            if (listSign.size > 1) {
                updateSign(binding.btnSign3, listSign[0])
                updateSign(binding.btnSign4, listSign[1])
                updateSign(binding.btnSign5, "no")
            } else if (listSign.size > 0) {
                updateSign(binding.btnSign3, listSign[0])
                updateSign(binding.btnSign4, "no")
                updateSign(binding.btnSign5, "no")
            } else {
                updateSign(binding.btnSign3, "no")
                updateSign(binding.btnSign4, "no")
                updateSign(binding.btnSign5, "no")
            }
        }
    }

    private fun resizeSign(size : Float){
        val view = binding.constraintLayout
        val constraintSet = ConstraintSet()
        constraintSet.clone(view)
        constraintSet.constrainPercentWidth(R.id.btn_sign3, size)
        constraintSet.constrainPercentWidth(R.id.btn_sign4, size)
        constraintSet.constrainPercentWidth(R.id.btn_sign5, size)
        constraintSet.applyTo(view)
        checkReSize025 = size == 0.25f
    }

    private fun updateSign(view: AppCompatImageView, signId: String) {

        when (signId) {
            "no" -> view.setBackgroundResource(R.drawable.sign_no)
            "12" -> {
                view.setBackgroundResource(R.drawable.ic_sign_gofa12)
                AppUtils.enqueueSound(context, R.raw.gofa_start_city)
            }
            "13" -> {
                view.setBackgroundResource(R.drawable.ic_sign_gofa13)
                AppUtils.enqueueSound(context, R.raw.gofa_end_city)
            }
            "14" -> {
                view.setBackgroundResource(R.drawable.ic_sign_gofa14)
                AppUtils.enqueueSound(context, R.raw.gofa_start_no_overtaking)
            }
            "15" -> {
                view.setBackgroundResource(R.drawable.ic_sign_gofa15)
                AppUtils.enqueueSound(context, R.raw.gofa_end_no_overtaking)
            }
            "21" -> {
                view.setBackgroundResource(R.drawable.ic_sign_gofa21)
                AppUtils.enqueueSound(context, R.raw.gofa_no_left)
            }
            "22" -> {
                view.setBackgroundResource(R.drawable.ic_sign_gofa22)
                AppUtils.enqueueSound(context, R.raw.gofa_no_right)
            }
            "23" -> {
                view.setBackgroundResource(R.drawable.ic_sign_gofa23)
                AppUtils.enqueueSound(context, R.raw.gofa_no_left_back)
            }
            "24" -> {
                view.setBackgroundResource(R.drawable.ic_sign_gofa24)
                AppUtils.enqueueSound(context, R.raw.gofa_no_right_back)
            }
            "25" -> {
                view.setBackgroundResource(R.drawable.ic_sign_gofa25)
                AppUtils.enqueueSound(context, R.raw.gofa_no_back)
            }
            "26" -> {
                view.setBackgroundResource(R.drawable.ic_sign_gofa26)
                AppUtils.enqueueSound(context, R.raw.gofa_no_back)
            }
            "28" -> view.setBackgroundResource(R.drawable.ic_sign_gofa28)
            "29" -> view.setBackgroundResource(R.drawable.ic_sign_gofa29)
            "30" -> view.setBackgroundResource(R.drawable.ic_sign_gofa30)
            "31" -> view.setBackgroundResource(R.drawable.ic_sign_gofa31)
            "32" -> {
                view.setBackgroundResource(R.drawable.ic_sign_gofa32)
                AppUtils.enqueueSound(context, R.raw.gofa_camera)
            }
            "33" -> {
                view.setBackgroundResource(R.drawable.ic_sign_gofa32)
                AppUtils.enqueueSound(context, R.raw.gofa_camera)
            }
            "34" -> {
                view.setBackgroundResource(R.drawable.ic_sign_gofa32)
                AppUtils.enqueueSound(context, R.raw.gofa_camera)
            }
        }
    }
}
