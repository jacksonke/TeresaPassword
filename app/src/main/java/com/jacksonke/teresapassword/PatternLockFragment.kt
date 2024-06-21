package com.jacksonke.teresapassword

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.andrognito.patternlockview.PatternLockView
import com.andrognito.patternlockview.listener.PatternLockViewListener
import com.andrognito.patternlockview.utils.PatternLockUtils
import com.jacksonke.teresapassword.databinding.FragmentPatternLockBinding


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class PatternLockFragment : Fragment() {

    companion object {
        const val TAG = "PatternLockFragment"
    }

    private var _binding: FragmentPatternLockBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val patternListener: PatternLockViewListener = object : PatternLockViewListener{
        override fun onStarted() {
            Log.d(TAG, "onStarted: ")
        }

        override fun onProgress(progressPattern: List<PatternLockView.Dot?>?) {
            Log.d(TAG, "onProgress: ")
        }

        override fun onComplete(pattern: List<PatternLockView.Dot>?) {
            Log.d(TAG, "onComplete: ")
            val intent = Intent(
                this@PatternLockFragment.activity,
                MainActivity::class.java
            )
            val secret = PatternLockUtils.patternToString(binding.patternLockView, pattern)
            intent.putExtra(Constants.KeySecret, secret)

            Generator.instance().setSecret(secret)

            startActivity(intent)
            this@PatternLockFragment.requireActivity().finish()
        }

        override fun onCleared() {
            Log.d(TAG, "onCleared: ")
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentPatternLockBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.patternLockView.addPatternLockListener(patternListener)
//        binding.buttonFirst.setOnClickListener {
//            findNavController().navigate(R.id.action_First2Fragment_to_Second2Fragment)
//        }
    }

    override fun onDestroyView() {
        binding.patternLockView.removePatternLockListener(patternListener)
        super.onDestroyView()
        _binding = null
    }
}