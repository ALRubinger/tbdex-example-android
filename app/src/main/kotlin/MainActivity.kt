package com.example.tbdexy

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.security.keystore.KeyProtection
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tbdex.sdk.httpclient.TbdexHttpClient
import tbdex.sdk.protocol.models.Offering
import web5.sdk.credentials.VerifiableCredential
import web5.sdk.crypto.InMemoryKeyManager
import web5.sdk.dids.methods.dht.DidDht
import java.security.KeyStore
import java.security.KeyStore.Entry
import java.security.KeyStore.PasswordProtection
import java.security.KeyStore.SecretKeyEntry
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

/**
 * Shows the basics of interacting with the the tbdex SDK in an android app.
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /*
         * This is a JWT that represents a Verifiable Credential (VC) prepared earlier
         */
        val signedVcJwt = "eyJ0eXAiOiJKV1QiLCJhbGciOiJFZERTQSIsImtpZCI6ImRpZDpkaHQ6a2ZkdGJjbTl6Z29jZjVtYXRmOWZ4dG5uZmZoaHp4YzdtZ2J3cjRrM3gzcXppYXVjcHA0eSMwIn0.eyJ2YyI6eyJAY29udGV4dCI6WyJodHRwczovL3d3dy53My5vcmcvMjAxOC9jcmVkZW50aWFscy92MSJdLCJ0eXBlIjpbIlZlcmlmaWFibGVDcmVkZW50aWFsIiwiRW1wbG95bWVudENyZWRlbnRpYWwiXSwiaWQiOiJ1cm46dXVpZDo4ZmQ1MjAzMC0xY2FmLTQ5NzgtYTM1ZC1kNDE3ZWI4ZTAwYjIiLCJpc3N1ZXIiOiJkaWQ6ZGh0OmtmZHRiY205emdvY2Y1bWF0ZjlmeHRubmZmaGh6eGM3bWdid3I0azN4M3F6aWF1Y3BwNHkiLCJpc3N1YW5jZURhdGUiOiIyMDIzLTEyLTIxVDE3OjAyOjAxWiIsImNyZWRlbnRpYWxTdWJqZWN0Ijp7ImlkIjoiZGlkOmRodDp5MzltNDhvem9ldGU3ejZmemFhbmdjb3M4N2ZodWgxZHppN2Y3andiamZ0N290c2toOXRvIiwicG9zaXRpb24iOiJTb2Z0d2FyZSBEZXZlbG9wZXIiLCJzdGFydERhdGUiOiIyMDIxLTA0LTAxVDEyOjM0OjU2WiIsImVtcGxveW1lbnRTdGF0dXMiOiJDb250cmFjdG9yIn0sImV4cGlyYXRpb25EYXRlIjoiMjAyMi0wOS0zMFQxMjozNDo1NloifSwiaXNzIjoiZGlkOmRodDprZmR0YmNtOXpnb2NmNW1hdGY5Znh0bm5mZmhoenhjN21nYndyNGszeDNxemlhdWNwcDR5Iiwic3ViIjoiZGlkOmRodDp5MzltNDhvem9ldGU3ejZmemFhbmdjb3M4N2ZodWgxZHppN2Y3andiamZ0N290c2toOXRvIn0.ntcgPOdXOatULWo-q6gkuhKmi5X3bzCONQY38t_rsC1hVhvvdAtmiz-ccoLIYUkjECRHIxO_UZbOKgn0EETBCA"
        val vc = VerifiableCredential.parseJwt(signedVcJwt)
        print(vc) // VerifiableCredential details
        print(vc.issuer) // VerifiableCredential issuer

        val vcTextView: TextView = findViewById(R.id.vcTextView)

        /*
         * Create a new did for this app, and store it in the AndroidKeyManager (encrypted prefs).
         * This should be done once, and the did should be stored for future use automatically.
         */
        val keyManager = AndroidKeyManager(applicationContext)
        val did = DidDht.create(keyManager)
        vcTextView.text = "this is my did: " + did.didDocument?.id



        // Use Coroutine to perform network operation in the background as required by android
        CoroutineScope(Dispatchers.IO).launch {
            try {
                /*
                 * This will talk to a PFI (liquidity node) and get the offerings available. The DID that is provided is from the PFI server.
                 * This is a list of offerings which we can render later on. See the OffersAdapter class for how it shows some of the offering fields.
                 */
                val off = TbdexHttpClient.getOfferings("did:ion:EiCVF_RwIWYYitxCFakM12UJQZROBRqS1EG98E-1GxYtcA:eyJkZWx0YSI6eyJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljS2V5cyI6W3siaWQiOiJkd24tc2lnIiwicHVibGljS2V5SndrIjp7ImNydiI6IkVkMjU1MTkiLCJrdHkiOiJPS1AiLCJ4IjoiNUZSd1ZnWjZXczUzZXFIUWNKeHgtUjRYVzFPa1lha2lMb0pHTE1nTnVfOCJ9LCJwdXJwb3NlcyI6WyJhdXRoZW50aWNhdGlvbiIsImFzc2VydGlvbk1ldGhvZCJdLCJ0eXBlIjoiSnNvbldlYktleTIwMjAifV0sInNlcnZpY2VzIjpbeyJpZCI6InBmaSIsInNlcnZpY2VFbmRwb2ludCI6Imh0dHBzOi8vZGVzaWducy1hZHZlcnRpc2VyLWFyY2hpdmUtbGFuZ3VhZ2UudHJ5Y2xvdWRmbGFyZS5jb20iLCJ0eXBlIjoiUEZJIn1dfX1dLCJ1cGRhdGVDb21taXRtZW50IjoiRWlBWW42NkFSbGJfd05ORzRLcDZJYXZJQzRIOUh6a2tBVFdOcGZiVnZPSVl5dyJ9LCJzdWZmaXhEYXRhIjp7ImRlbHRhSGFzaCI6IkVpQjJ3N0w1SmMzQmVGTktqOTI2ZEI2ajZMM0xmSjNOSk9pZ3FBck1Tek9tbHciLCJyZWNvdmVyeUNvbW1pdG1lbnQiOiJFaUJNLWFYNWFDQlhaNFZpQ0VhMHBUU1gwZVIzWkVyaVZxbmI5c1lJeDRyUXB3In19")


                // Update UI on the main thread
                withContext(Dispatchers.Main) {
                    val offersRecyclerView: RecyclerView = findViewById(R.id.offersRecyclerView)
                    val offersAdapter = OffersAdapter(off)
                    offersRecyclerView.adapter = offersAdapter
                }

            } catch (e: Exception) {

                withContext(Dispatchers.Main) {
                    vcTextView.text = vcTextView.text.toString() + "\nUnable to load the offers, check logcat"
                }

            }
        }





    }
}

class OffersAdapter(private val offers: List<Offering>) : RecyclerView.Adapter<OffersAdapter.OfferViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OfferViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_offer, parent, false)
        return OfferViewHolder(view)
    }

    override fun onBindViewHolder(holder: OfferViewHolder, position: Int) {
        val offer = offers[position]
        holder.bind(offer)
    }

    override fun getItemCount(): Int = offers.size

    class OfferViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
        private val exchangeRateTextView: TextView = itemView.findViewById(R.id.exchangeRateTextView)
        private val currenciesTextView: TextView = itemView.findViewById(R.id.currenciesTextView)

        fun bind(offer: Offering) {
            descriptionTextView.text = offer.data.description
            exchangeRateTextView.text = "Exchange Rate: ${offer.data.payoutUnitsPerPayinUnit}"
            currenciesTextView.text = "Payout: ${offer.data.payoutCurrency.currencyCode}, Payin: ${offer.data.payinCurrency.currencyCode}"
        }
    }
}
