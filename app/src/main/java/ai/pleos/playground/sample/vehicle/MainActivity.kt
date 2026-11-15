package ai.pleos.playground.sample.vehicle

import ai.pleos.playground.sample.vehicle.ui.screen.SampleApp
import ai.pleos.playground.sample.vehicle.ui.theme.VehicleSampleAppTheme
import ai.pleos.playground.sample.vehicle.community.CommunityApp
import androidx.compose.runtime.*
import androidx.activity.compose.BackHandler
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            var sdkScreen by remember { mutableStateOf(false) }
            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_STOP) sdkScreen = false
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            }
            BackHandler(enabled = sdkScreen) { sdkScreen = false }

            VehicleSampleAppTheme {
                if (!sdkScreen) {
                    CommunityApp(onOpenSdk = { sdkScreen = true })
                } else {
                    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
                    SampleApp(
                        uiState = uiState,
                        onOpenDomain = viewModel::showApiExplorer,
                        onSelectFeature = viewModel::selectFeature,
                        onSearchFeatures = viewModel::searchFeatures,
                        onRunCapability = viewModel::runFeatureCapability,
                        onRunRead = viewModel::runFeatureRead,
                        onRunSet = viewModel::runFeatureSet,
                        onStartUpdates = viewModel::startFeatureUpdates,
                        onStopUpdates = viewModel::stopFeatureUpdates,
                        onBackToFeatureList = viewModel::backToFeatureList,
                        onDismissError = viewModel::dismissError
                    )
                }
            }
        }
    }
}
