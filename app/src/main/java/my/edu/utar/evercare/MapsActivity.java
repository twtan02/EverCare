package my.edu.utar.evercare;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private PlacesViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        ImageView backButton = findViewById(R.id.backButton);

        // Set OnClickListener to the back button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle back button click event
                onBackPressed();
            }
        });

        viewModel = new ViewModelProvider(this).get(PlacesViewModel.class);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        viewModel.getNearbyPlaces();
        viewModel.getPlaces().observe(this, places -> {
            for (Place place : places) {
                mMap.addMarker(new MarkerOptions().position(new LatLng(place.getLat(), place.getLng())).title(place.getName()));
            }
        });
    }

    public static class PlacesViewModel extends ViewModel {

        private MutableLiveData<List<Place>> places = new MutableLiveData<>();

        public void getNearbyPlaces() {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.submit(() -> {
                String apiKey = "AIzaSyCAssxxUDeUevzN8Wn5JazMRTjMhgdQTGA";
                String urlString = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                        "location=4.2105,101.9758" + // Malaysia coordinates
                        "&radius=50000" + // 50km radius
                        "&type=hospital" +
                        "&key=" + apiKey;

                try {
                    URL url = new URL(urlString);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    StringBuilder response = new StringBuilder();
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    List<Place> placesList = parsePlacesResponse(response.toString());
                    places.postValue(placesList);

                    // Log out all the places
                    for (Place place : placesList) {
                        Log.d("PlacesViewModel", "Place: " + place.getName() + " - Lat: " + place.getLat() + " - Lng: " + place.getLng());
                    }

                    connection.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        public LiveData<List<Place>> getPlaces() {
            return places;
        }

        private List<Place> parsePlacesResponse(String response) {
            List<Place> placesList = new ArrayList<>();
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray resultsArray = jsonObject.getJSONArray("results");
                for (int i = 0; i < resultsArray.length(); i++) {
                    JSONObject placeObject = resultsArray.getJSONObject(i);
                    JSONObject locationObject = placeObject.getJSONObject("geometry").getJSONObject("location");
                    double lat = locationObject.getDouble("lat");
                    double lng = locationObject.getDouble("lng");
                    String name = placeObject.getString("name");
                    placesList.add(new Place(name, lat, lng));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return placesList;
        }
    }

    public static class Place {
        private String name;
        private double lat;
        private double lng;

        public Place(String name, double lat, double lng) {
            this.name = name;
            this.lat = lat;
            this.lng = lng;
        }

        public String getName() {
            return name;
        }

        public double getLat() {
            return lat;
        }

        public double getLng() {
            return lng;
        }
    }
}
