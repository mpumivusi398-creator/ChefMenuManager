package com.example.chefapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.chefapp.ui.theme.ChefMenuManagerTheme

data class MenuItem(
    val id: Int,
    var name: String,
    var description: String,
    var price: Double,
    var course: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChefMenuManagerTheme {
                ChefMenuManagerApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChefMenuManagerApp() {
    val context = LocalContext.current
    var menuList by remember { mutableStateOf(listOf<MenuItem>()) }
    var itemName by remember { mutableStateOf("") }
    var itemDescription by remember { mutableStateOf("") }
    var itemPrice by remember { mutableStateOf("") }
    var selectedCourse by remember { mutableStateOf("Main Course") }
    val courseOptions = listOf("Starter", "Main Course", "Dessert")
    var searchText by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    val filterOptions = listOf("All", "Starter", "Main Course", "Dessert")
    var editingItem by remember { mutableStateOf<MenuItem?>(null) }

    fun clearForm() {
        itemName = ""
        itemDescription = ""
        itemPrice = ""
        selectedCourse = "Main Course"
        editingItem = null
    }

    fun getTotalItems(): Int = menuList.size
    fun getAveragePrice(): Double {
        if (menuList.isEmpty()) return 0.0
        return menuList.sumOf { it.price } / menuList.size
    }
    fun getCountByCourse(course: String): Int {
        return menuList.count { it.course == course }
    }

    val filteredList = menuList.filter { item ->
        val matchesSearch = item.name.contains(searchText, ignoreCase = true)
        val matchesFilter = selectedFilter == "All" || item.course == selectedFilter
        matchesSearch && matchesFilter
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Chef's Menu Manager - Part 3", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(12.dp))

        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = "Menu Statistics", style = MaterialTheme.typography.titleMedium)
                Text(text = "Total Items: ${getTotalItems()}")
                Text(text = "Average Price: R %.2f".format(getAveragePrice()))
                Text(text = "Starters: ${getCountByCourse("Starter")} | Main: ${getCountByCourse("Main Course")} | Dessert: ${getCountByCourse("Dessert")}")
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(value = searchText, onValueChange = { searchText = it }, label = { Text("Search by name...") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            filterOptions.forEach { filter ->
                FilterChip(selected = selectedFilter == filter, onClick = { selectedFilter = filter }, label = { Text(filter) })
            }
        }
        Button(onClick = { searchText = ""; selectedFilter = "All"; Toast.makeText(context, "Search and filter cleared", Toast.LENGTH_SHORT).show() }) { Text("Clear Search / Filter") }
        Spacer(modifier = Modifier.height(12.dp))

        Text(text = if (editingItem == null) "Add New Item" else "Edit Item: ${editingItem?.name}", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(value = itemName, onValueChange = { itemName = it }, label = { Text("Dish Name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = itemDescription, onValueChange = { itemDescription = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = itemPrice, onValueChange = { itemPrice = it }, label = { Text("Price (e.g. 85.50)") }, modifier = Modifier.fillMaxWidth())

        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(value = selectedCourse, onValueChange = {}, readOnly = true, label = { Text("Course") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, modifier = Modifier.menuAnchor().fillMaxWidth())
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                courseOptions.forEach { course ->
                    DropdownMenuItem(text = { Text(course) }, onClick = { selectedCourse = course; expanded = false })
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            val priceValue = itemPrice.toDoubleOrNull()
            if (itemName.isBlank()) { Toast.makeText(context, "Error: Please enter a dish name", Toast.LENGTH_SHORT).show(); return@Button }
            if (itemDescription.isBlank()) { Toast.makeText(context, "Error: Please enter a description", Toast.LENGTH_SHORT).show(); return@Button }
            if (priceValue == null || priceValue <= 0) { Toast.makeText(context, "Error: Please enter a valid price", Toast.LENGTH_SHORT).show(); return@Button }
            if (editingItem == null) {
                val newId = if (menuList.isEmpty()) 1 else menuList.maxOf { it.id } + 1
                val newItem = MenuItem(newId, itemName.trim(), itemDescription.trim(), priceValue, selectedCourse)
                menuList = menuList + newItem
                Toast.makeText(context, "Success: Item added", Toast.LENGTH_SHORT).show()
            } else {
                menuList = menuList.map { item ->
                    if (item.id == editingItem!!.id) item.copy(name = itemName.trim(), description = itemDescription.trim(), price = priceValue, course = selectedCourse) else item
                }
                Toast.makeText(context, "Success: Item updated", Toast.LENGTH_SHORT).show()
            }
            clearForm()
        }, modifier = Modifier.fillMaxWidth()) {
            Text(if (editingItem == null) "Add Item" else "Update Item")
        }
        if (editingItem != null) {
            TextButton(onClick = { clearForm() }) { Text("Cancel Edit") }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "Menu Items (${filteredList.size})", style = MaterialTheme.typography.titleMedium)
        LazyColumn {
            items(filteredList) { item ->
                MenuItemCard(item = item,
                    onEditClick = {
                        editingItem = item
                        itemName = item.name
                        itemDescription = item.description
                        itemPrice = item.price.toString()
                        selectedCourse = item.course
                    },
                    onDeleteClick = {
                        menuList = menuList.filter { it.id != item.id }
                        Toast.makeText(context, "Deleted: ${item.name}", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

@Composable
fun MenuItemCard(item: MenuItem, onEditClick: () -> Unit, onDeleteClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = "${item.name} - R %.2f".format(item.price), style = MaterialTheme.typography.titleMedium)
            Text(text = "Course: ${item.course}")
            Text(text = item.description)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onEditClick) { Text("Edit") }
                OutlinedButton(onClick = onDeleteClick) { Text("Delete") }
            }
        }
    }
}