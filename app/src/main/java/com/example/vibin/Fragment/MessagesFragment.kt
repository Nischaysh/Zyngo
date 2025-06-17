package com.example.vibin.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.vibin.BottomSheet.ChatUserBottomSHeet
import com.example.vibin.BottomSheet.NotificationBottomSheet
import com.example.vibin.R
import com.example.vibin.databinding.FragmentHomeBinding
import com.example.vibin.databinding.FragmentMessagesBinding

class MessagesFragment : Fragment() {

    private var _binding: FragmentMessagesBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMessagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.fabNewChat.setOnClickListener {
            val bottomsheet = ChatUserBottomSHeet()
            bottomsheet.show(parentFragmentManager,bottomsheet.tag)
        }
    }

}