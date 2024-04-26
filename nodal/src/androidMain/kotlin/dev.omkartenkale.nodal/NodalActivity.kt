package dev.omkartenkale.nodal

import android.widget.FrameLayout
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.compose.setContent
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.lifecycleScope
import dev.omkartenkale.nodal.Node.Companion.createRootNode
import dev.omkartenkale.nodal.compose.UI
import dev.omkartenkale.nodal.misc.RemovalRequest
import kotlinx.coroutines.launch

public abstract class NodalActivity : AppCompatActivity() {

    public lateinit var ui: UI
    public lateinit var contentNode: Node
    protected inline fun <reified T : Node> setContentNode(
        container: FrameLayout = findViewById(android.R.id.content),
        noinline dependencyDeclaration: DependencyDeclaration = {},
    ) {
//        val finalNodeConfigBuilder: NodeConfigBuilder = {
//            param<RemovalRequest>(RemovalRequest{ finish() })
////            param(localsharedpref)
////            param(savedinstancestate)
////            param(Unit)
//            apply(nodeConfigBuilder)
//        }

        contentNode = createRootNode<T>{
//            provides<Timber> { Timber()  }
//            provides<TreeVisualiser> { TreeVisualiser()  }
//            provides<StringProvider> { StringProvider()  }
//            provides<AnalyticsLogger> { AnalyticsLogger()  }
//            provides<KoroutineDispatcher> { KoroutineDispatcher()  }
//            provides<FileManager> { FileManager()  }
//
//            provides<SesionInfo> { SesionInfo()  }
//            provides<RideInfo> { RideInfo()  }
//            provides<SelectedCountry> { SelectedCountry()  }
//            provides<DeeplinkResolver> { DeeplinkResolver()  }
//            provides{ SystemNotificationManager()  }
//            provides{ SystemEventListener()  }
//            provides{ BuildVarient()  }
//            provides{ NodalConfig()  }
//
//            //only to children and not subchildren
//            provides<UIContainer> { UIContainer()  }
//            provides<SomeDeeplink> { SomeDeeplink()  }
//            provides { ProductDetails()  }
//
//            providesSelf<RemovalRequest>(RemovalRequest{ finish() })

            provides<NodalConfig>{ NodalConfig(true) }
            provides<OnBackPressedDispatcher>{ onBackPressedDispatcher }
            provides<UI>{ UI().also {
                ui = it
                container.addView(ComposeView(this@NodalActivity).also { setContent { ui.drawLayers() } })
            } }
            providesSelf<RemovalRequest> {
                RemovalRequest {
                    finish()
                }
            }
            include(dependencyDeclaration)
        }.apply { dispatchAdded() }
        container.addView(ComposeView(this).also { setContent { ui.drawLayers() } })
    }

    @CallSuper
    override fun onResume() {
        super.onResume()
        if (::ui.isInitialized) {
            lifecycleScope.launch {
                ui.dispatchFocusChanged(isFocused = true)
            }
        }
    }

    @CallSuper
    override fun onPause() {
        super.onPause()
        if (::ui.isInitialized) {
            lifecycleScope.launch {
                ui.dispatchFocusChanged(isFocused = false)
            }
        }
    }

    @CallSuper
    override fun onDestroy() {
        super.onDestroy()
        if (::contentNode.isInitialized) {
            contentNode.dispatchRemoved()
        }
    }
}