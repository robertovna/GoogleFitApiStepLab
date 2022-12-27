package com.gayane.fitapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.gayane.fitapp.databinding.FragmentViewStepBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.*
import com.google.android.gms.fitness.request.DataReadRequest
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

class ViewStepFragment : Fragment() {
    companion object {
        private const val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1313
    }
    private var _binding: FragmentViewStepBinding? = null
    private val binding get() = _binding!!

    private val navigationArgs: ViewStepFragmentArgs by navArgs()

    private val fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_WRITE)
        .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_WRITE)
        .build()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentViewStepBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val account = GoogleSignIn.getAccountForExtension(requireContext(), fitnessOptions)

        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                this,
                GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
                account,
                fitnessOptions)
        } else {
            if (navigationArgs.count != 0) {
                addSteps(navigationArgs.count)
            }
            else {
                accessGoogleFit()
            }
        }
    }

    private fun addSteps(count: Int) {
        val dataSource = DataSource.Builder()
            .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
            .setType(DataSource.TYPE_RAW)
            .setAppPackageName(requireContext())
            .build()

        val dataPoint =
            DataPoint.builder(dataSource)
                .setField(Field.FIELD_STEPS, count)
                .setTimeInterval(
                    LocalDateTime.now().atZone(ZoneId.systemDefault()).minusSeconds(30).toEpochSecond(),
                    LocalDateTime.now().atZone(ZoneId.systemDefault()).minusSeconds(10).toEpochSecond(),
                    TimeUnit.SECONDS,
                )
                .build()

        val dataSet = DataSet.builder(dataSource)
            .add(dataPoint)
            .build()

        val account = GoogleSignIn.getAccountForExtension(requireContext(), fitnessOptions)
        Fitness.getHistoryClient(requireContext(), account)
            .insertData(dataSet)
            .addOnSuccessListener {
                Log.i("gayane", "success")
                accessGoogleFit()
            }
            .addOnFailureListener {
                Log.i("gayane", "fail")
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            Activity.RESULT_OK -> when (requestCode) {
                GOOGLE_FIT_PERMISSIONS_REQUEST_CODE -> accessGoogleFit()
                else -> {}
            }
            else -> {}
        }
    }

    private fun accessGoogleFit() {
        val end = LocalDateTime.now()
        val start = end.minusYears(1)
        val endSeconds = end.atZone(ZoneId.systemDefault()).toEpochSecond()
        val startSeconds = start.atZone(ZoneId.systemDefault()).toEpochSecond()

        val readRequest = DataReadRequest.Builder()
            .aggregate(DataType.AGGREGATE_STEP_COUNT_DELTA)
            .setTimeRange(startSeconds, endSeconds, TimeUnit.SECONDS)
            .bucketByTime(1, TimeUnit.DAYS)
            .build()

        val account = GoogleSignIn.getAccountForExtension(requireContext(), fitnessOptions)
        Fitness.getHistoryClient(requireActivity(), account)
            .readData(readRequest)
            .addOnSuccessListener { response ->
                var countSteps = 0
                for (bucket in response.buckets) {
                    for (dataSet in bucket.dataSets) {
                        for (dataPoint in dataSet.dataPoints) {
                            countSteps += dataPoint.getValue(Field.FIELD_STEPS).toString().toInt()
                        }
                    }
                }
                bindData(countSteps)
            }
    }

    private fun bindData(result: Int?) {
        val steps = result ?: -123
        binding.stepCount.text = steps.toString()
        binding.addSteps.setOnClickListener {
            val action = ViewStepFragmentDirections.actionViewStepFragmentToEditStepInfoFragment()
            findNavController().navigate(action)
        }

        binding.deleteSteps.setOnClickListener {
            deleteSteps()
        }
    }

    private fun deleteSteps() {
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}