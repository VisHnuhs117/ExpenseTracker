package com.vishnuhs.expensetracker.utils

import com.vishnuhs.expensetracker.ml.ReceiptData

object NavigationDataHolder {
    var receiptData: ReceiptData? = null
    var imagePath: String? = null

    fun setData(data: ReceiptData?, path: String?) {
        receiptData = data
        imagePath = path
    }

    fun clearData() {
        receiptData = null
        imagePath = null
    }
}