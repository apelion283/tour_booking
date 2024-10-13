package com.project17.tourbooking.helper.firestore_helper

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.project17.tourbooking.constant.ACCOUNT_ROLE
import com.project17.tourbooking.constant.PAYMENT_STATUS
import com.project17.tourbooking.constant.TICKET_TYPE
import com.project17.tourbooking.helper.firebase_cloud_helper.FirebaseCloudHelper
import com.project17.tourbooking.models.Account
import com.project17.tourbooking.models.Bill
import com.project17.tourbooking.models.Category
import com.project17.tourbooking.models.CategoryOfTour
import com.project17.tourbooking.models.CoinPackage
import com.project17.tourbooking.models.Customer
import com.project17.tourbooking.models.Destination
import com.project17.tourbooking.models.Review
import com.project17.tourbooking.models.Ticket
import com.project17.tourbooking.models.Tour
import com.project17.tourbooking.models.TourBooking
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.UUID


object FirestoreHelper {

    @SuppressLint("StaticFieldLeak")
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()


    suspend fun uploadImageToFirebase(uri: Uri): String? {
        val storageRef = storage.reference
        val imageRef = storageRef.child("images/${UUID.randomUUID()}.jpg")

        return try {
            val uploadTask = imageRef.putFile(uri).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await()
            downloadUrl.toString()
        } catch (e: Exception) {
            null
        }
    }

    //---------------------------Coin Booking Bills-----------------------------
    fun getBillById(billId: String, callback: (Bill?) -> Unit) {
        db.collection("bills").document(billId).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val bill = document.toObject(Bill::class.java)
                    callback(bill)
                } else {
                    callback(null)
                }
            }
            .addOnFailureListener {
                callback(null)
            }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun Date.toLocalDate(): LocalDate {
        return this.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    }

    suspend fun getOrdersByUser(): Map<String, Int> {
        val bills = FirebaseFirestore.getInstance().collection("bills").get().await()
        val userOrderCount = mutableMapOf<String, Int>()

        for (document in bills.documents) {
            val email = document.getString("email") ?: continue
            userOrderCount[email] = userOrderCount.getOrDefault(email, 0) + 1
        }

        return userOrderCount
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getOrdersByDate(): Map<String, Int> {
        val bills = FirebaseFirestore.getInstance().collection("bills").get().await()
        val dateOrderCount = mutableMapOf<String, Int>()

        for (document in bills.documents) {
            val createdDate =
                document.getTimestamp("createdDate")?.toDate()?.toLocalDate() ?: continue
            val date = createdDate.toString()
            dateOrderCount[date] = dateOrderCount.getOrDefault(date, 0) + 1
        }

        return dateOrderCount
    }

    private suspend fun getOrdersByCategory(): Map<String, Int> {
        val billDetails = FirebaseFirestore.getInstance().collection("billDetails").get().await()
        val tickets = FirebaseFirestore.getInstance().collection("tickets").get().await()

        val categoryOrderCount = mutableMapOf<String, Int>()
        val ticketCategoryMap = tickets.documents.associateBy { it.id }
            .mapValues { it.value.getString("categoryId") ?: "" }

        for (document in billDetails.documents) {
            val ticketId = document.getString("ticketId") ?: continue
            val categoryId = ticketCategoryMap[ticketId] ?: continue
            categoryOrderCount[categoryId] = categoryOrderCount.getOrDefault(categoryId, 0) + 1
        }

        return categoryOrderCount
    }


    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getOrdersByMonth(): Map<String, Int> {
        val bills = FirebaseFirestore.getInstance().collection("bills").get().await()
        val monthOrderCount = mutableMapOf<String, Int>()

        for (document in bills.documents) {
            val createdDate =
                document.getTimestamp("createdDate")?.toDate()?.toLocalDate() ?: continue
            val month = "${createdDate.year}-${createdDate.monthValue}"
            monthOrderCount[month] = monthOrderCount.getOrDefault(month, 0) + 1
        }

        return monthOrderCount
    }

    suspend fun getTopSellingCategories(limit: Int): List<String> {
        val ordersByCategory = getOrdersByCategory()
        val sortedCategories = ordersByCategory.entries.sortedByDescending { it.value }
        return sortedCategories.take(limit).map { it.key }
    }


    fun deleteBillsAndDetails(billIds: List<String>, callback: (Boolean, Exception?) -> Unit) {
        val batch = FirebaseFirestore.getInstance().batch()

        billIds.forEach { billId ->
            val billRef = FirebaseFirestore.getInstance().collection("bills").document(billId)
            batch.delete(billRef)

            FirebaseFirestore.getInstance().collection("billDetails")
                .whereEqualTo("billId", billId)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    querySnapshot.forEach { document ->
                        val billDetailRef = document.reference
                        batch.delete(billDetailRef)
                    }
                    // Commit the batch after adding all delete operations
                    batch.commit().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            callback(true, null)
                        } else {
                            callback(false, task.exception)
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    callback(false, exception)
                }
        }
    }

    // --------------------- Category ---------------------
    private val categoriesCollection = db.collection("categories")

    private suspend fun getCategoryByCategoryId(categoryId: String): Category{
        return try{
            val document = categoriesCollection.document(categoryId).get().await()
            document.toObject(Category::class.java)?.copy(id = document.id)!!
        }
        catch (e: Exception){
            Category("", "", "")
        }
    }

    fun getAllCategories(onResult: (List<Category>) -> Unit) {
        db.collection("categories")
            .get()
            .addOnSuccessListener { result ->
                val categories = result.documents.mapNotNull { doc ->
                    doc.toObject(Category::class.java)?.copy(id = doc.id)
                }
                onResult(categories)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }


    suspend fun createCategory(category: Category) {
        try{
            val imageUrl = FirebaseCloudHelper.uploadImage(imageUri = category.image, pathString = "category_images")
            val newCategoryId = categoriesCollection.document().id
            val newCategoryData = mapOf(
                "name" to category.name,
                "image" to imageUrl
            )
            categoriesCollection.document(newCategoryId).set(newCategoryData).await()
        }
        catch (e: Exception){
            e.printStackTrace()
        }
    }

    suspend fun updateCategoryByCategoryId(categoryId: String, updatedCategory: Category) {
        val updateCategoryData = mapOf(
            "name" to updatedCategory.name,
            "image" to updatedCategory.image
        )
        try{
            categoriesCollection.document(categoryId).set(updateCategoryData).await()
        }
        catch (e: Exception){
            e.printStackTrace()
        }
    }

    suspend fun deleteCategoryByCategoryId(categoryId: String) {
        try{
            val imageURL = getCategoryByCategoryId(categoryId).image
            categoriesCollection.document(categoryId).delete().await()
            if (imageURL.isNotEmpty()) {
                FirebaseCloudHelper.deleteImageFromUrl(imageURL)
            }
        }
        catch(e: Exception){
            e.printStackTrace()
        }
    }

    // --------------------- Tour ---------------------
    private val toursCollection = db.collection("tours")

    suspend fun getAllTours(): List<Tour> {
        return try {
            val snapshot = toursCollection.get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Tour::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getToursFromRating(rating: Double): List<Tour> {
        val loadedTours = getAllTours()
        return loadedTours.filter { it.averageRating > rating }
    }

    suspend fun getTourByTourId(tourId: String): Tour? {
        return try {
            val document = toursCollection.document(tourId).get().await()
            document.toObject(Tour::class.java)?.copy(id = document.id)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getTopToursIsMostBooked(limit: Long): List<Tour> {
        return try {
            val snapshot = toursCollection.orderBy("bookingCount", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Tour::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getAverageRatingByTourId(tourId: String): Double {
        return try {
            val snapshot = toursCollection.document(tourId).get().await()
            val tour = snapshot.toObject(Tour::class.java)
            tour?.averageRating ?: 0.0
        } catch (e: Exception) {
            0.0
        }
    }

    suspend fun getTourByTicketId(ticketId: String): Tour?{
        return try{
            val tourId = ticketsCollection.document(ticketId).get().await().getString("tourId")
            val document = toursCollection.document(tourId!!).get().await()
            document.toObject(Tour::class.java)?.copy(id = document.id)!!
        }
        catch (e: Exception){
            e.printStackTrace()
            null
        }
    }

    private suspend fun updateTourAverageRating(tourId: String, newRating: Double, oldRating: Double? = null){
        val reviewCount = reviewsCollection.whereEqualTo("tourId", tourId).get().await().size()
        val tourToUpdate = getTourByTourId(tourId)
        if(oldRating == null){
            val newAverageRating = (((tourToUpdate?.averageRating?.times(reviewCount - 1)) ?: 0.0) + newRating) / reviewCount
            toursCollection.document(tourId).update("averageRating", newAverageRating)
        }else {
            val newAverageRating = (((tourToUpdate?.averageRating?.times(reviewCount)) ?: 0.0) + newRating - oldRating) / reviewCount
            toursCollection.document(tourId).update("averageRating", newAverageRating)
        }
    }

    private fun updateTourBookingCount(tourId: String, quantity: Int) {
        toursCollection.document(tourId).get().addOnSuccessListener { document ->
            val countOfTourBeforeUpdate = document.getLong("bookingCount")

            if (countOfTourBeforeUpdate != null) {
                toursCollection.document(tourId).update("bookingCount", countOfTourBeforeUpdate + quantity)
                    .addOnSuccessListener {
                    }
                    .addOnFailureListener { e ->
                        e.printStackTrace()
                    }
            }
        }.addOnFailureListener { e ->
            e.printStackTrace()
        }
    }

    suspend fun updateTour(tourId: String, tour: Tour, price: Pair<Double, Int>, categoriesOfTour: List<Category>): Boolean {
        return try{
            val tourNeedToUpdate = getTourByTourId(tourId)
            if(tourNeedToUpdate != tour){
                toursCollection.document(tourId).update(
                    mapOf(
                        "name" to tour.name,
                        "openRegistrationDate" to tour.openRegistrationDate,
                        "closeRegistrationDate" to tour.closeRegistrationDate,
                        "cancellationDeadline" to tour.cancellationDeadline,
                        "startDate" to tour.startDate,
                        "slotQuantity" to tour.slotQuantity,
                        "image" to tour.image,
                        "bookingCount" to tour.bookingCount,
                        "averageRating" to tour.averageRating,
                        "description" to tour.description,
                        "destinationId" to tour.destinationId
                    )
                )
            }
            val ticketNeedToUpdate = getTicketOfTourByTourId(tourId)
            if(ticketNeedToUpdate == null){
                createTicket(Ticket("",TICKET_TYPE.ADULT, price.first.toLong(), price.second.toLong(), tourId))
            }
            else{
                if(Pair(ticketNeedToUpdate.moneyPrice.toDouble(), ticketNeedToUpdate.coinPrice.toInt()) != price){
                    updateTicket(
                        getTicketIdOfTourByTourId(tourId), Ticket(
                            id = "",
                            tourId = tourId,
                            moneyPrice = price.first.toLong(),
                            coinPrice = price.second.toLong(),
                            ticketType = TICKET_TYPE.ADULT
                        ))
                }
            }

            val categoriesOfTourNeedToUpdate = getCategoriesOfTourByTourId(tourId)
            if(categoriesOfTourNeedToUpdate != categoriesOfTour){
                updateCategoryOfTour(tourId, categoriesOfTour)
            }
            true
        }
        catch (e: Exception){
            false
        }
    }

    suspend fun deleteTourByTourId(tourId: String) {
        val imageURL = getTourByTourId(tourId)?.image
        toursCollection.document(tourId).delete().await()
        deleteCategoryOfTourByTourId(tourId)
        deleteTicketByTourId(tourId)
        if (imageURL != null) {
            FirebaseCloudHelper.deleteImageFromUrl(imageURL)
        }
    }

    suspend fun createNewTour(tour: Tour, price: Pair<Double, Int>, categoriesOfTour: List<Category>): Boolean{
        return try{
            val newTourId = toursCollection.document().id
            val newTourData = mapOf(
                "name" to tour.name,
                "openRegistrationDate" to tour.openRegistrationDate,
                "closeRegistrationDate" to tour.closeRegistrationDate,
                "cancellationDeadline" to tour.cancellationDeadline,
                "startDate" to tour.startDate,
                "slotQuantity" to tour.slotQuantity,
                "image" to tour.image,
                "bookingCount" to tour.bookingCount,
                "averageRating" to tour.averageRating,
                "description" to tour.description,
                "destinationId" to tour.destinationId
            )
            toursCollection.document(newTourId).set(newTourData).await()
            createTicket(Ticket(
                id = "",
                tourId = newTourId,
                moneyPrice = price.first.toLong(),
                coinPrice = price.second.toLong(),
                ticketType = TICKET_TYPE.ADULT
            ))
            categoriesOfTour.forEach { category ->
                createCategoryOfTour(
                    CategoryOfTour(
                        id = "",
                        tourId = newTourId,
                        categoryId = category.id
                    )
                )
            }
            true
        }
        catch (e: Exception){
            e.printStackTrace()
            false
        }
    }


    //-----------------------CategoryOfTour-------------
    private val categoryOfTourCollection = db.collection("categoryOfTours")

    private suspend fun getCategoryIdListOfTourByTourId(tourId: String): List<String> {
        return try {
            val snapshot = categoryOfTourCollection.whereEqualTo("tourId", tourId).get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.getString("categoryId")
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getCategoriesOfTourByTourId(tourId: String): List<Category> {
        return try {
            val categoryIdList = getCategoryIdListOfTourByTourId(tourId)

            categoryIdList.mapNotNull { id ->
                val snapshot = categoriesCollection.document(id).get().await()
                val category = snapshot.toObject(Category::class.java)?.copy(id = snapshot.id)

                category
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun getTourIdListOfCategoryByCategoryId(categoryId: String): List<String> {
        return try {
            val snapshot =
                categoryOfTourCollection.whereEqualTo("categoryId", categoryId).get().await()
            val tourIds = snapshot.documents.mapNotNull { doc ->
                doc.getString("tourId")
            }
            tourIds
        } catch (e: Exception) {
            emptyList()
        }
    }


    suspend fun getToursOfCategoryByCategoryId(categoryId: String): List<Tour> {
        return try {
            val tourIdList = getTourIdListOfCategoryByCategoryId(categoryId)
            tourIdList.mapNotNull { id ->
                val snapshot = toursCollection.document(id).get().await()
                snapshot.toObject(Tour::class.java)?.copy(id = snapshot.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getToursByCategoryIdNameStarRatingAndPriceRange(categoryId: String, name: String, star: Int, moneyRangePrice: Pair<Int, Int>?): List<Tour> {
        return try {
            val tourIdList = if (categoryId.isNotEmpty()) {
                getTourIdListOfCategoryByCategoryId(categoryId)
            } else {
                toursCollection.get().await().documents.mapNotNull { it.id }
            }

            var tourList = tourIdList.mapNotNull { id ->
                val snapshot = toursCollection.document(id).get().await()
                 snapshot.toObject(Tour::class.java)?.copy(id = snapshot.id)
            }

            if(name.isNotEmpty()){
                tourList = tourList.filter { tour ->
                    tour.name.contains(name, ignoreCase = true)

                }
            }

            if(moneyRangePrice != null){
                tourList = tourList.filter { tour ->
                    val tourPrice = getTicketPriceByTourId(tour.id)
                    tourPrice.first.toInt() in moneyRangePrice.first..moneyRangePrice.second
                }
            }

            if(star > 0){
                tourList = tourList.filter { tour ->
                    tour.averageRating >= star.toDouble()
                }
            }

            tourList
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun createCategoryOfTour(categoryOfTour: CategoryOfTour) {
        val newCategoryOfTourId = categoryOfTourCollection.document().id
        val newCategoryOfTourData = mapOf(
            "tourId" to categoryOfTour.tourId,
            "categoryId" to categoryOfTour.categoryId
        )

        try {
            categoryOfTourCollection.document(newCategoryOfTourId).set(newCategoryOfTourData).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private suspend fun deleteCategoryOfTourByTourId(tourId: String){
        val querySnapshot = categoryOfTourCollection.whereEqualTo("tourId", tourId).get().await()
        for (document in querySnapshot.documents) {
            document.reference.delete().await()
        }
    }

    private suspend fun updateCategoryOfTour(tourId: String, categoriesOfTour: List<Category>) {
        val currentCategories = getCategoriesOfTourByTourId(tourId)
        if (categoriesOfTour != currentCategories) {
            deleteCategoryOfTourByTourId(tourId)
            for (category in categoriesOfTour) {
                createCategoryOfTour(
                    CategoryOfTour(
                        id = "",
                        tourId = tourId,
                        categoryId = category.id
                    )
                )
            }

        }
    }


    // --------------------- Ticket ---------------------
    private val ticketsCollection = db.collection("tickets")

    private suspend fun getTicketIdOfTourByTourId(tourId: String): String {
        return try {
            val snapshot = ticketsCollection.whereEqualTo("tourId", tourId)
                .whereEqualTo("ticketType", TICKET_TYPE.ADULT)
                .get().await()
            if (snapshot.documents.isNotEmpty()) {
                snapshot.documents.first().id
            } else {
                "null"
            }

        } catch (e: Exception) {
            "null"
        }
    }

    private fun getTourIdByTicketId(ticketId: String, onSuccess: (String?) -> Unit) {
        ticketsCollection
            .document(ticketId).get()
            .addOnSuccessListener { document ->
                val tourId = document.getString("tourId")
                onSuccess(tourId)
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
                onSuccess(null)
            }
    }

    suspend fun getTicketOfTourByTourId(tourId: String): Ticket? {
        return try {
            val ticketId = getTicketIdOfTourByTourId(tourId)
            val document = ticketsCollection.document(ticketId).get().await()

            if (document.exists()) {
                val ticketData = document.data
                Ticket(
                    id = document.id,
                    tourId = ticketData?.get("tourId") as? String ?: "",
                    ticketType = ticketData?.get("ticketType") as? TICKET_TYPE ?: TICKET_TYPE.ADULT,
                    moneyPrice = ticketData?.get("moneyPrice") as? Long ?: 0L,
                    coinPrice = ticketData?.get("coinPrice") as? Long ?: 0L
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("Error", "Exception in getTicketOfTourByTourId: ${e.message}")
            null
        }
    }


    suspend fun getTicketPriceByTourId(tourId: String): Pair<Long, Long> {
        val ticket = getTicketOfTourByTourId(tourId)
        if (ticket == null) {
            return Pair(ticket?.moneyPrice ?: 0, ticket?.coinPrice ?: 0)
        }
        return Pair(ticket.moneyPrice ?: 0, ticket.coinPrice ?: 0)
    }

    private fun createTicket(ticket: Ticket): Result<Unit> {
        return try {
            val newTicketId = ticketsCollection.document().id
            val newTicketData = mapOf(
                "tourId" to ticket.tourId,
                "ticketType" to ticket.ticketType,
                "moneyPrice" to ticket.moneyPrice,
                "coinPrice" to ticket.coinPrice,
                "ticketType" to ticket.ticketType
            )
            ticketsCollection.document(newTicketId).set(newTicketData)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun updateTicket(docId: String, ticket: Ticket): Result<Unit> {
        return try {
            ticketsCollection.document(docId).set(
                mapOf(
                    "tourId" to ticket.tourId,
                    "ticketType" to ticket.ticketType,
                    "moneyPrice" to ticket.moneyPrice,
                    "coinPrice" to ticket.coinPrice)
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun deleteTicketByTourId(tourId: String): Result<Unit> {
        return try {
            val ticketId = getTicketIdOfTourByTourId(tourId)
            db.collection("tickets").document(ticketId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --------------------- Bill ---------------------
    private val billsCollection = db.collection("bills")


    fun createCoinPackageBill(bill: Bill, onSuccess: (String) -> Unit): Task<Void> {
        return try {
            val documentReference = billsCollection.document()
            val newBillData = mapOf(
                "coinPackageId" to bill.coinPackageId,
                "createdDate" to bill.createdDate,
                "paymentStatus" to bill.paymentStatus,
                "totalAmount" to bill.totalAmount,
                "couponId" to bill.couponId
            )
            documentReference.set(newBillData).addOnSuccessListener {
                onSuccess(documentReference.id)
            }.addOnFailureListener { e ->
                e.printStackTrace()
            }
        }
        catch (e: Exception){
            Tasks.forException(e)
        }
    }

    suspend fun getCoinPackageBillByBillId(billId: String): Bill{
        return try{
            val document = billsCollection.document(billId).get().await()
            document.toObject(Bill::class.java)!!.copy(id = document.id)
        }
        catch (e: Exception){
            Bill()
        }
    }

    fun deleteBillDetailsByBillId(billId: String, callback: (Boolean, Exception?) -> Unit) {
        val billDetailsRef = FirebaseFirestore.getInstance().collection("billDetails")
        billDetailsRef.whereEqualTo("billId", billId).get().addOnSuccessListener { querySnapshot ->
            val deleteTasks = querySnapshot.documents.map { doc ->
                doc.reference.delete()
            }
            Tasks.whenAllComplete(deleteTasks).addOnSuccessListener {
                callback(true, null)
            }.addOnFailureListener { exception ->
                Log.e(
                    "com.project17.tourbooking.helper.firestorehelper.FirestoreHelper",
                    "Error deleting bill details: ${exception.localizedMessage}"
                )
                callback(false, exception)
            }
        }.addOnFailureListener { exception ->
            Log.e(
                "com.project17.tourbooking.helper.firestorehelper.FirestoreHelper",
                "Error fetching bill details: ${exception.localizedMessage}"
            )
            callback(false, exception)
        }
    }


    suspend fun updateBill(docId: String, bill: Bill): Result<Unit> {
        return try {
            db.collection("bills").document(docId).set(bill).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteBill(docId: String): Result<Unit> {
        return try {
            db.collection("bills").document(docId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    // --------------------- Account ---------------------
    private val accountsCollection = db.collection("accounts")

    suspend fun isUsernameExists(username: String): Boolean {
        return try {
            val snapshot = accountsCollection.whereEqualTo("userName", username).get().await()
            !snapshot.isEmpty
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getAllAccounts(): List<Account> {
        return try{
            val snapshot = accountsCollection.get().await()
             snapshot.documents.mapNotNull { it.toObject(Account::class.java) }
        }catch (e: Exception){
            e.printStackTrace()
            emptyList<Account>()
        }
    }


    suspend fun getUserRole(accountId: String): String?{
        return try {
            val snapshot = accountsCollection.document(accountId).get().await()
            snapshot.getString("role")
        }
        catch (e: Exception){
            ACCOUNT_ROLE.USER.toString()
        }
    }

    suspend fun getCustomerIdByAccountId(accountId: String): Result<String> {
        return try {
            val snapshot = accountsCollection.document(accountId).get().await()
            val customerId = snapshot.getString("customerId")
            if (customerId != null) {
                Result.success(customerId)
            } else {
                Result.failure(NullPointerException("Customer ID not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAccountByAccountId(accountId: String): Account? {
        return try {
            val snapshot = accountsCollection.document(accountId).get().await()
            snapshot.toObject(Account::class.java)?.copy(id = snapshot.id)
        } catch (e: Exception) {
            Account()
        }
    }

    fun createAccount(account: Account) {
        val accountData = mapOf(
            "userName" to account.userName,
            "avatar" to account.avatar,
            "role" to account.role,
            "customerId" to account.customerId,
            "coin" to account.coin,
            "status" to account.status
        )
          accountsCollection.document(account.id).set(accountData)
    }

    fun updateAccountCoin(accountId: String, quantity: Int, isAdd: Boolean, onSuccess: () -> Unit) {
        accountsCollection.document(accountId).get().addOnSuccessListener { document ->
            val coinOfAccountBeforeUpdate = document.getLong("coin")

            if (coinOfAccountBeforeUpdate != null) {
                val newCoinAmount = if(isAdd) coinOfAccountBeforeUpdate + quantity else coinOfAccountBeforeUpdate - quantity

                accountsCollection.document(accountId).update("coin", newCoinAmount)
                    .addOnSuccessListener {
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        e.printStackTrace()
                    }
            }
        }.addOnFailureListener { e ->
            e.printStackTrace()
        }
    }


    suspend fun updateAccountByAccountId(accountId: String, accountToUpdate: Account) {
        try {
            Log.d(
                "com.project17.tourbooking.helper.firestorehelper.FirestoreHelper",
                "Attempting to update account with username: $accountId"
            )

            val updateAccountData = mapOf(
                "userName" to accountToUpdate.userName,
                "avatar" to accountToUpdate.avatar,
                "role" to accountToUpdate.role
            )

            accountsCollection.document(accountId).update(updateAccountData).await()
            Log.d(
                "com.project17.tourbooking.helper.firestorehelper.FirestoreHelper",
                "Successfully updated account with username: $accountId"
            )

        } catch (e: Exception) {
            Log.e(
                "com.project17.tourbooking.helper.firestorehelper.FirestoreHelper",
                "Error updating account with username: $accountId",
                e
            )
        }
    }


    suspend fun deleteAccountByAccountId(accountId: String) {
//        try {
//            Log.d(
//                "com.project17.tourbooking.helper.firestorehelper.FirestoreHelper",
//                "Attempting to delete account with email: $accountId"
//            )
//
//            // Query Firestore to find the document with the matching email
//            val querySnapshot = accountsCollection.whereEqualTo("email", accountId).get().await()
//
//            if (querySnapshot.isEmpty) {
//                Log.d(
//                    "com.project17.tourbooking.helper.firestorehelper.FirestoreHelper",
//                    "No account found with email: $accountId"
//                )
//                return
//            }
//
//            // Delete all documents matching the email
//            for (document in querySnapshot.documents) {
//                val documentId = document.id
//                accountsCollection.document(documentId).delete().await()
//                Log.d(
//                    "com.project17.tourbooking.helper.firestorehelper.FirestoreHelper",
//                    "Successfully deleted account with ID: $documentId"
//                )
//            }
//        } catch (e: Exception) {
//            Log.e(
//                "com.project17.tourbooking.helper.firestorehelper.FirestoreHelper",
//                "Error deleting account with email: $accountId",
//                e
//            )
//        }
    }

    fun getAvatarUrlFromAccountId(accountId: String, callback: (String?) -> Unit) {
        accountsCollection.document(accountId).get()
            .addOnSuccessListener { account ->
                if (account.exists()) {
                    val avatarUrl = account.getString("avatar")
                    callback(avatarUrl)
                } else {
                    callback(null)
                }

            }
            .addOnFailureListener {
                callback(null)
            }
    }

    fun getUserNameByAccountId(accountId: String, callback: (String?) -> Unit) {
        accountsCollection.document(accountId).get()
            .addOnSuccessListener { account ->
                if (account.exists()) {
                    val userName = account.getString("userName")
                    callback(userName)
                } else {
                    callback(null)
                }

            }
            .addOnFailureListener {
                callback(null)
            }
    }



    // --------------------- Customer ---------------------
    private val customersCollection = db.collection("customers")

    fun createCustomer(customer: Customer): String {
        val customerId = customersCollection.document().id
        val newCustomer = customer.copy(id = customerId)
        customersCollection.document(customerId).set(newCustomer)
        return customerId
    }

    suspend fun getAllCustomers(): Result<Map<String, Customer>> {
        return try {
            val snapshot = customersCollection.get().await()
            val customers =
                snapshot.documents.associate { it.id to it.toObject(Customer::class.java)!! }
            Result.success(customers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCustomerNameByAccountId(accountId: String, callback: (String?) -> Unit) {
        val result = getCustomerIdByAccountId(accountId)
        val customerId = if (result.isSuccess) result.getOrNull() else null

        if (customerId != null) {
            customersCollection.document(customerId).get()
                .addOnSuccessListener { querySnapshot ->
                    val customer = querySnapshot.toObject(Customer::class.java)
                    callback(customer?.fullName)
                }
                .addOnFailureListener { exception ->
                    callback(null)
                    Log.e("GetCustomerError", "Error fetching customer: ${exception.message}")
                }
        }
    }

    fun getCustomerInfoByCustomerId(customerId: String, callback: (Customer?) -> Unit) {
        customersCollection
            .document(customerId)
            .get()
            .addOnSuccessListener { result ->
                if (!result.exists()) {
                    callback(null)
                } else {
                    val customer = result.toObject(Customer::class.java)
                    val returnCustomer = customer?.copy(
                        customer.id,
                        customer.fullName,
                        customer.gender,
                        customer.dateOfBirth,
                        customer.address,
                        customer.phoneNumber
                    )
                    callback(returnCustomer)
                }
            }
            .addOnFailureListener { exception ->
                callback(null)
            }
    }

    suspend fun updateCustomer(docId: String, customer: Customer): Result<Unit> {
        return try {
            db.collection("customers").document(docId).set(customer).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCustomer(docId: String): Result<Unit> {
        return try {
            db.collection("customers").document(docId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --------------------- Review ---------------------
    private val reviewsCollection = db.collection("reviews")

    suspend fun getReviewsByTourId(tourId: String): List<Review> {
        return try {
            val snapshot = reviewsCollection.whereEqualTo("tourId", tourId).get().await()
            snapshot.documents.mapNotNull { it.toObject(Review::class.java)?.copy(id = it.id) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getReviewByTourIdAndAccountIdAndTourBookingBillId(tourId: String, accountId: String, tourBookingBillId: String): Review? {
        return try{
            if(isExistReviewOfTour(tourId, accountId, tourBookingBillId)){
                val document = reviewsCollection
                    .whereEqualTo("tourId", tourId)
                    .whereEqualTo("accountId", accountId)
                    .whereEqualTo("tourBookingBillId", tourBookingBillId)
                    .get().await().firstOrNull()
                document?.toObject(Review::class.java)?.copy(id = document.id)
            }
            else {
                null
            }
        }
        catch (e: Exception){
            null
        }
    }

    suspend fun isExistReviewOfTour(tourId: String, accountId: String, billId: String): Boolean{
        return try{
            val document = reviewsCollection
                .whereEqualTo("tourId", tourId)
                .whereEqualTo("accountId", accountId)
                .whereEqualTo("tourBookingBillId", billId)
                .get().await()
            document.size() > 0
            }
        catch (e: Exception){
            false
        }
    }

    suspend fun createReview(review: Review): Result<Unit> {
        return try {
            val reviewData = mapOf(
            "rating" to review.rating,
            "comment" to review.comment,
            "tourId" to review.tourId,
            "accountId" to review.accountId,
            "tourBookingBillId" to review.tourBookingBillId,
            "createdDate" to review.createdDate
        )

        val newReviewId = reviewsCollection.document().id

        reviewsCollection.document(newReviewId).set(reviewData)
        updateTourAverageRating(review.tourId, review.rating)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
        }
    }

    suspend fun updateReviewByReviewId(reviewId: String, review: Review, oldReview: Review): Result<Unit> {
        return try {
            val reviewData = mapOf(
                "rating" to review.rating,
                "comment" to review.comment,
                "tourId" to review.tourId,
                "accountId" to review.accountId,
                "tourBookingBillId" to review.tourBookingBillId,
                "createdDate" to review.createdDate
            )
            reviewsCollection.document(reviewId).set(reviewData).await()
            updateTourAverageRating(review.tourId, review.rating, oldReview.rating)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteReview(docId: String): Result<Unit> {
        return try {
            reviewsCollection.document(docId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //---------------DESTINATION-------------------
    private val destinationCollection = db.collection("destinations")

    suspend fun getAllDestination(): List<Destination> {
        return try {
            val snapshot = destinationCollection.get().await()
            snapshot.documents.mapNotNull { it.toObject(Destination::class.java)?.copy(id = it.id) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getDestinationById(destinationId: String): Destination {
        return try {
            val document = destinationCollection.document(destinationId).get().await()
            if (document.exists()) {
                document.toObject(Destination::class.java)?.copy(id = destinationId)
                    ?: Destination()
            } else {
                Destination()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Destination()
        }
    }

    suspend fun createDestination(destination: Destination) {
        try {
            val destinationData = mapOf(
                "location" to destination.location,
                "description" to destination.description
            )
            val destinationId = destinationCollection.document().id
            destinationCollection.document(destinationId).set(destinationData).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun updateDestinationByDestinationId(destinationId: String, destinationToUpdate: Destination) {
        try {
            val destinationData = mapOf(
                "location" to destinationToUpdate.location,
                "description" to destinationToUpdate.description
            )
            destinationCollection.document(destinationId).set(destinationData).await()
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun deleteDestinationByDestinationId(destinationId: String){
        try {
            destinationCollection.document(destinationId).delete().await()
        }
        catch (e: Exception){
            e.printStackTrace()
        }
    }

    //--------------------WishList------------------
    private val wishListCollection = db.collection("wishlistItems")

    suspend fun getTourIdListOfWishListByAccountId(accountId: String): List<String> {
        return try {
            val snapshot = wishListCollection.whereEqualTo("accountId", accountId).get().await()
            snapshot.documents.mapNotNull { it.getString("tourId") }
        } catch (e: Exception) {
            emptyList()
        }
    }

    //-----------------------CoinPackage------------------
    private val coinPackageCollection = db.collection("coinPackages")

    suspend fun getCoinPackageByCoinPackageId(coinPackageId: String): CoinPackage?{
        return try {
            val document = coinPackageCollection.document(coinPackageId).get().await()
            document.toObject(CoinPackage::class.java)?.copy(id = document.id)
        }catch (e: Exception){
            null
        }
    }

    suspend fun getAllCoinPackage(): List<CoinPackage> {
        return try {
            val snapshot = coinPackageCollection.get().await()
            snapshot.documents.mapNotNull { it.toObject(CoinPackage::class.java)?.copy(id = it.id) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun createCoinPackage(coinPackage: CoinPackage){
        try {
            val coinPackageId = coinPackageCollection.document().id
            val coinPackageData = mapOf(
                "name" to coinPackage.name,
                "description" to coinPackage.description,
                "coinValue" to coinPackage.coinValue,
                "price" to coinPackage.price
            )
            coinPackageCollection.document(coinPackageId).set(coinPackageData).await()
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    suspend fun updateCoinPackageByCoinPackageId(coinPackageId: String, coinPackage: CoinPackage){
        try{
            val coinPackageData = mapOf(
                "name" to coinPackage.name,
                "description" to coinPackage.description,
                "coinValue" to coinPackage.coinValue,
                "price" to coinPackage.price
            )
            coinPackageCollection.document(coinPackageId).set(coinPackageData).await()
        }
        catch (e: Exception){
            e.printStackTrace()
        }
    }

    suspend fun deleteCoinPackageByCoinPackageId(coinPackageId: String){
        try {
            coinPackageCollection.document(coinPackageId).delete().await()
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //-----------------------TOUR BOOKING-------------------
    private val tourBookingCollection = db.collection("tourBookings")

    suspend fun getTourBookingBillByBillId(billId: String): TourBooking {
        return try{
           val document = tourBookingCollection.document(billId).get().await()
            document.toObject(TourBooking::class.java)!!.copy(id = document.id)
        }
        catch (e: Exception) {
            TourBooking()
        }
    }

    suspend fun getTourBookingsByAccountId(accountId: String): List<TourBooking>{
        return try{
            tourBookingCollection.whereEqualTo("accountId", accountId).get()
                .await().documents.mapNotNull { it.toObject(TourBooking::class.java)?.copy(id = it.id) }
        }
        catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun createTourBookingBill(tourBooking: TourBooking): String {
        return try {
            val documentReference = tourBookingCollection.document()
            val newTourBookingData = mapOf(
                "accountId" to tourBooking.accountId,
                "ticketId" to tourBooking.ticketId,
                "quantity" to tourBooking.quantity,
                "total" to tourBooking.total,
                "valueType" to tourBooking.valueType,
                "bookingDate" to tourBooking.bookingDate,
                "paymentStatus" to tourBooking.paymentStatus,
                "couponId" to tourBooking.couponId,
                "cancellationDeadline" to tourBooking.cancellationDeadline,
                "startTourDate" to tourBooking.startTourDate
            )
            val tour = getTourByTicketId(tourBooking.ticketId)
            if (tour != null) {
                updateTourBookingCount(tour.id, tourBooking.quantity)
            }
            documentReference.set(newTourBookingData)
            documentReference.id
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    suspend fun cancelTourBookingBill(billId: String): Result<Unit> {
        return try {
            tourBookingCollection.document(billId).update("paymentStatus", PAYMENT_STATUS.CANCELLED).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

