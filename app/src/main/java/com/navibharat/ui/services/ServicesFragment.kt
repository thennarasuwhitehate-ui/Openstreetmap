package com.navibharat.ui.services

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.chip.ChipGroup
import com.navibharat.R
import com.navibharat.features.services.ServiceType
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class ServicesFragment : Fragment() {

    private val viewModel: ServicesViewModel by viewModels()
    private lateinit var chipGroup: ChipGroup

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Timber.i("ServicesFragment created")
        return inflater.inflate(R.layout.fragment_services, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chipGroup = view.findViewById(R.id.service_chips)

        // Setup chip selection listener
        chipGroup.setOnCheckedChangeListener { _, checkedId ->
            val serviceType = when (checkedId) {
                R.id.chip_petrol -> ServiceType.PETROL_BUNK
                R.id.chip_ev -> ServiceType.EV_CHARGER
                R.id.chip_mechanic -> ServiceType.MECHANIC
                R.id.chip_rest -> ServiceType.REST_AREA
                else -> ServiceType.PETROL_BUNK
            }

            viewModel.searchServices(serviceType)
            Timber.i("Searching for: ${serviceType.displayName}")
        }

        // Set default selection
        chipGroup.check(R.id.chip_petrol)
    }
}
