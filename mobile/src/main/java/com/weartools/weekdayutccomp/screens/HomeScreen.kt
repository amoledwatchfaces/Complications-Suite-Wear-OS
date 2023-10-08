package com.weartools.weekdayutccomp.screens

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shop
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.weartools.weekdayutccomp.MainViewModel
import com.weartools.weekdayutccomp.R
import com.weartools.weekdayutccomp.ui.components.ImageSwitchBox
import com.weartools.weekdayutccomp.ui.components.NavigationDefaults
import com.weartools.weekdayutccomp.utils.openAmoledWebPage
import com.weartools.weekdayutccomp.utils.openGuideLink
import com.weartools.weekdayutccomp.utils.openPlayStore
import com.weartools.weekdayutccomp.utils.openPlayStorePortfolio
import com.weartools.weekdayutccomp.utils.sendFeedbackEmail
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    context: Context,
    viewModel: MainViewModel,
    scope: CoroutineScope,
    isWatchConnected: State<Boolean>,
    listState: LazyListState
) {
    val state by viewModel.loaderStateStateFlow.collectAsState()

    val pullRefreshState = rememberPullRefreshState(
        refreshing = state,
        onRefresh = { scope.launch {
            viewModel.findAllWearDevices(context)
        } }
    )


    Column(
        Modifier.fillMaxSize()
    ) {
        CenterAlignedTopAppBar(
            actions = {
                IconButton(
                    modifier = Modifier.padding(end = 5.dp),
                    onClick = { context.openPlayStore() }) {
                    Icon(
                        imageVector = Icons.Default.Shop,
                        contentDescription = "Play Store",
                        tint = colorScheme.onPrimaryContainer
                    )
                }
            },
            title = {
                Text(
                    fontWeight = FontWeight.Medium,
                    text = stringResource(id = R.string.app_name)
                )
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = colorScheme.background,
            )
        )

        LazyColumn(
            state = listState,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)

        ) {
            // Show the top app bar on top level destinations.
            //val destination = appState.currentTopLevelDestination
            //if (destination != null) {
            item {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {

                        ImageSwitchBox()
                        Image(
                            modifier = Modifier
                                .size(300.dp),
                            alignment = Alignment.Center,
                            painter = painterResource(id = R.drawable.fgs_glow),
                            contentDescription = "Frame"
                        )
                    }
            }
            // TODO: IMPLEMENT BASIC APP LOGIC

            item {
                ElevatedCard(
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 6.dp
                    ),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(bottom = 20.dp, top = 20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(id = R.string.welcome),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(start = 16.dp, top = 10.dp, bottom = 0.dp),
                            textAlign = TextAlign.Center,
                        )
                        TextButton(
                            modifier = Modifier.padding(top = 10.dp, bottom = 0.dp, end= 10.dp),
                            onClick = { context.openGuideLink()})
                        {
                            Text(
                                textDecoration = TextDecoration.Underline,
                                text = stringResource(id = R.string.installation_guides),
                                color = colorScheme.onPrimaryContainer)
                        }
                    }

                    Text(
                        text = stringResource(id = R.string.intro),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Left,
                    )
                    if(isWatchConnected.value){
                        Text(
                            text = stringResource(id = R.string.connected),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Left,
                        )
                        Text(
                            text = stringResource(id = R.string.uninstall),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Left,
                        )
                    }
                    else
                    {
                        Text(
                            text = stringResource(id = R.string.no_devices),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Left,
                        )
                        Text(
                            text = stringResource(id = R.string.try_reconnect),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Left,
                        )
                    }

                }
            }

            item {
                ElevatedCard(
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 6.dp
                    ),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(bottom = 20.dp, top = 20.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.note),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp),
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = stringResource(id = R.string.note_text),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Left,
                    )

                    TextButton(
                        modifier = Modifier.padding(start = 4.dp, end = 16.dp, top = 0.dp, bottom = 4.dp),
                        onClick = {context.sendFeedbackEmail()}) {
                        Text(
                            text = stringResource(id = R.string.support),
                            color = colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Left,
                        )
                    }
                }


                // PORTFOLIO
                Text(
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .fillMaxWidth(0.9f),
                    text = stringResource(id = R.string.check_portfolio),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                )
                TextButton(
                    modifier = Modifier.padding(start = 4.dp, end = 16.dp, top = 12.dp, bottom = 4.dp),
                    onClick = {context.openPlayStorePortfolio()}) {
                    Text(
                        text = stringResource(id = R.string.dev_page),
                        color = colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            // AMOLEDWATCHFACES:COM
            item { TextButton(
                modifier = Modifier
                    .padding(top = 20.dp, bottom = 40.dp)
                    .wrapContentSize(),
                onClick = { context.openAmoledWebPage() }) {
                Text(
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    fontStyle = FontStyle.Italic,
                    text = stringResource(id = R.string.website),
                    color = Color.Gray)
            } }
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding()
            .pullRefresh(pullRefreshState)
    ){
        PullRefreshIndicator(
            refreshing = state,
            state = pullRefreshState,
            modifier = Modifier
                .padding(top = 60.dp)
                .align(alignment = Alignment.TopCenter),
            contentColor = colorScheme.primaryContainer,
            backgroundColor = NavigationDefaults.navigationSelectedItemColor(),
        )
    }

}