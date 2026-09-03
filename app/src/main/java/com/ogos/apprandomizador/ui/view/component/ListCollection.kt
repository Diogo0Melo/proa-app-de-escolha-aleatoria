package com.ogos.apprandomizador.ui.view.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ogos.apprandomizador.R
import com.ogos.apprandomizador.model.ItemList
import com.ogos.apprandomizador.ui.format.TimeAgoFormatter

@Composable
fun ListCollection(
    onNavigateToActive: (index: Long) -> Unit,
    modifier: Modifier = Modifier,
    allItemsList: List<ItemList>,
) {
    LazyColumn(
        modifier = modifier
    ) {
        items(count = allItemsList.size) { index ->
            val item = allItemsList[index]
            val scrollState = rememberScrollState()
            val lastUse = TimeAgoFormatter.updateLastUse(item.dateTimeHistory.lastOrNull())
            val createdAt = TimeAgoFormatter.formatCreatedAt(item.createdAt)
            Card(
                onClick = { onNavigateToActive(item.id) },
                modifier = Modifier
                    .padding(12.dp, 8.dp)
                    .height(136.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.verticalScroll(scrollState),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            val times = if (item.items.size >= 7) item.items.size else 7
                            repeat(
                                times = times,
                            ) { currentIndex ->
                                val circularIndex = currentIndex % item.items.size
                                val currentSubItem = item.items[circularIndex]
                                Box(
                                    modifier = Modifier.width(108.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(16.dp),
                                        color = Color(currentSubItem.color)
                                    ) {
                                        Text(
                                            text = currentSubItem.name,
                                            modifier = Modifier.wrapContentHeight(Alignment.CenterVertically),
                                            style = TextStyle(
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Black,
                                                textAlign = TextAlign.Center,
                                                drawStyle = Stroke(
                                                    miter = 10f,
                                                    width = 8f,
                                                    join = StrokeJoin.Round
                                                ),
                                                color = Color.Black
                                            )
                                        )
                                        Text(
                                            text = currentSubItem.name,
                                            modifier = Modifier.wrapContentHeight(Alignment.CenterVertically),
                                            style = TextStyle(
                                                textAlign = TextAlign.Center,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color.White
                                            )
                                        )
                                    }
                                }
                            }
                        }
                        Column(
                            modifier = Modifier
                                .padding(6.dp, 0.dp)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.SpaceAround,
                        ) {
                            Text(
                                text = item.topic,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(text = stringResource(R.string.items_count, item.items.size))
                            Text(text = stringResource(R.string.uses_count, item.uses))
                            Text(text = stringResource(R.string.last_use, lastUse))
                            Text(text = stringResource(R.string.created_at, createdAt))
                        }
                    }
                    Box() {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = stringResource(R.string.edit_item),
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}