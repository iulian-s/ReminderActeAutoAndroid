package com.example.reminderacteautoandroid.screen

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.reminderacteautoandroid.service.VehicleApiService
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.exp

@RequiresApi(Build.VERSION_CODES.O)
fun formatRelativeDate(isoDateTimeString: String?): String {
    if (isoDateTimeString.isNullOrEmpty()) return ""

    return try {
        val localDate = when {
            isoDateTimeString.contains("T") ->
                LocalDateTime.parse(isoDateTimeString).toLocalDate()
            else ->
                LocalDate.parse(isoDateTimeString)
        }

        localDate.format(
            DateTimeFormatter.ofPattern("dd MMM yyyy", Locale("ro", "RO"))
        )
    } catch (e: Exception) {
        println("Parse error: ${e.message}")
        isoDateTimeString
    }
}
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun VehicleCard(
    vehicle: VehicleApiService.VehicleResponseDTO,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
//            .clickable { onCardClick() },
        ,elevation = CardDefaults.cardElevation(4.dp)
    ){

        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically){
                Text(
                    text = "${vehicle.brand} ${vehicle.model}",
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.weight(1f))
                Box{
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.MoreVert, "Options")
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = {Text("Edit")},
                            onClick = {
                                expanded = false
                                onEditClick()
                            }
                        )
                        DropdownMenuItem(
                            text = {Text("Sterge")},
                            onClick = {
                                expanded = false
                                onDeleteClick()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            for(doc in vehicle.documents){
                Row(verticalAlignment = Alignment.CenterVertically){
                    val formattedDate = formatRelativeDate(doc.expiryDate)
                    Text(
                        text = "${ doc.type } - expira: $formattedDate"
                    )
                }
            }
        }
    }
}