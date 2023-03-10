package com.gayane.fitapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.gayane.fitapp.databinding.FragmentEditStepInfoBinding
import com.gayane.fitapp.databinding.FragmentViewStepBinding

class EditStepInfoFragment : Fragment() {
    private var _binding: FragmentEditStepInfoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditStepInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindData()
    }

    private fun bindData() {
        binding.addSteps.setOnClickListener {
            val action = EditStepInfoFragmentDirections.actionEditStepInfoFragmentToViewStepFragment(
                binding.count.text.toString().toInt()
            )
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}