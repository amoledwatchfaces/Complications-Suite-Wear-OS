package com.weartools.weekdayutccomp.screens

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Shop2
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.weartools.weekdayutccomp.MainViewModel
import com.weartools.weekdayutccomp.R
import com.weartools.weekdayutccomp.utils.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoScreen(
    context: Context,
    viewModel: MainViewModel

) {

    val listState = rememberLazyListState()

    Column(Modifier.fillMaxSize()) {
        // Show the top app bar on top level destinations.
        //val destination = appState.currentTopLevelDestination
        //if (destination != null) {
        CenterAlignedTopAppBar(
            actions = {
                IconButton(
                    modifier = Modifier.padding(end = 5.dp),
                    onClick = { context.openPlayStorePortfolio() }) {
                    Icon(
                        imageVector = Icons.Default.Shop2,
                        contentDescription = "Play Store Portfolio",
                        tint = colorScheme.onPrimaryContainer
                    )
                }
            },
            title = { Text(fontWeight = FontWeight.Medium, text = stringResource(id = R.string.info)) },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.Transparent,
            )
        )

        LazyColumn(
            state = listState,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()

        ) {
            // Show the top app bar on top level destinations.
            //val destination = appState.currentTopLevelDestination
            //if (destination != null) {
            item { Text(
                modifier = Modifier.padding(bottom = 10.dp),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
                text = "${stringResource(id = R.string.app_name)} ${stringResource(id = R.string.wear_os_watch_face)}") }

            item {
                ElevatedCard(
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 6.dp
                ),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(bottom = 20.dp, top = 20.dp)
            ){
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TextButton(
                        onClick = { context.openPrivacyPolicyLink() }) {
                        Text(
                            fontWeight = FontWeight.Medium,
                            style = MaterialTheme.typography.bodyMedium,
                            text = stringResource(id = R.string.privacy), color = colorScheme.onPrimaryContainer)
                    }
                    TextButton(
                        onClick = { context.sendFeedbackEmail() }) {
                        Text(
                            fontWeight = FontWeight.Medium,
                            style = MaterialTheme.typography.bodyMedium,
                            text = stringResource(id = R.string.feedback), color = colorScheme.onPrimaryContainer)
                    }
                }

            }
            }


            item { Text(
                modifier = Modifier.padding(bottom = 10.dp, top = 0.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                text = stringResource(id = R.string.liked_watch_face),) }
            item {
                Button(
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = colorScheme.primaryContainer,
                        contentColor = colorScheme.onPrimaryContainer

                    ),
                    onClick = { viewModel.showRateDialog(context)}) {
                    Text(
                        text = stringResource(id = R.string.leave_review),
                    )
                    Icon(
                        modifier = Modifier.padding(start = 14.dp),
                        imageVector = Icons.Default.RateReview,
                        contentDescription = null,
                    )

                }
            }



            item { Text(
                modifier = Modifier.padding(bottom = 10.dp, top = 30.dp),
                text = stringResource(id = R.string.follow_us), fontWeight = FontWeight.Medium) }
            item { Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(
                    modifier = Modifier.weight(0.33f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(onClick = { context.openTelegramSocialLink()  }) {
                        Icon(
                            tint = colorScheme.onPrimaryContainer,
                            imageVector = ImageVector.vectorResource(id = R.drawable.social_telegram),
                            contentDescription = null,
                        )
                    }
                    Text(
                        fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.bodyMedium,
                        text = "Telegram",
                        color = colorScheme.onPrimaryContainer)

                }
                Column(
                    modifier = Modifier.weight(0.33f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(onClick = { context.openGithubSocialLink() }) {
                        Icon(
                            tint = colorScheme.onPrimaryContainer,
                            imageVector = ImageVector.vectorResource(id = R.drawable.social_github),
                            contentDescription = null,
                        )
                    }
                    Text(
                        fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.bodyMedium,
                        text = "GitHub",
                        color = colorScheme.onPrimaryContainer)
                }
                Column(
                    modifier = Modifier.weight(0.33f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(onClick = { context.openBuyMeACoffeeSocialLink() }) {
                        Icon(
                            tint = colorScheme.onPrimaryContainer,
                            imageVector = ImageVector.vectorResource(id = R.drawable.social_buymeacoffee),
                            contentDescription = null,
                        )
                    }
                    Text(
                        fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.bodyMedium,
                        text = "Buy us a coffee",
                        color = colorScheme.onPrimaryContainer)
                }
            } }

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

}