package be.ucll.workloadplanner;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

// Hoofdscherm waarin de verschillende fragmenten geladen worden
public class MainActivity extends AppCompatActivity {

    private NavController navController;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private String userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        setSupportActionBar(findViewById(R.id.toolbar));

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container_view);
        navController = navHostFragment.getNavController();

        logCurrentUserId();
    }

    // Controle ingelogde user om te checken of de login gewerkt heeft
    private void logCurrentUserId() {
        FirebaseUser user = getCurrentUser();
        if (user != null) {
            String phoneNumber = user.getPhoneNumber();
            if (phoneNumber != null) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("users").whereEqualTo("userId", phoneNumber)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                                String userId = documentSnapshot.getString("userId");
                                Log.d("MainActivity", "User ID: " + userId);
                                getUserRole(userId); // Call to get user role after user ID retrieval
                            } else {
                                Log.d("MainActivity", "User document does not exist");
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e("MainActivity", "Error getting user document", e);
                        });
            } else {
                Log.d("MainActivity", "No phone number associated with the current user");
            }
        } else {
            Log.d("MainActivity", "No user is currently authenticated");
        }
    }

    // Huidige gebruiker ophalen
    private FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    // Voor het weergeven van het menu rechtsboven
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        // Hide or show menu items based on user role
        MenuItem homeItem = menu.findItem(R.id.action_home);
        MenuItem addTicketItem = menu.findItem(R.id.action_addTicket);
        MenuItem logoutItem = menu.findItem(R.id.action_logout);

        if (userRole != null && userRole.equals("Project Manager")) {
            homeItem.setVisible(true);
            addTicketItem.setVisible(true);
            logoutItem.setVisible(true);
        } else if (userRole != null && userRole.equals("Member")) {
            homeItem.setVisible(true);
            addTicketItem.setVisible(false);
            logoutItem.setVisible(true);
        } else {
            addTicketItem.setVisible(false);
            logoutItem.setVisible(false);
        }

        return true;
    }

    // Instellen van de opties van het menu
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_home) {
            navController.navigate(R.id.action_any_fragment_to_tickets);
            return true;
        }

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container_view);
        if (navHostFragment != null) {
            Fragment currentFragment = navHostFragment.getChildFragmentManager().getPrimaryNavigationFragment();
            // Now you have the current fragment


            Log.d("MainActivity", "Current fragment: " + currentFragment.getClass().getSimpleName());

            if (currentFragment instanceof TicketsFragment || currentFragment instanceof AddTicketFragment || currentFragment instanceof UpdateTicketFragment) {
                if (item.getItemId() == R.id.action_addTicket) {
                    Log.d("MainActivity", "Adding ticket...");
                    navController.navigate(R.id.action_any_fragment_to_addTicket);
                    return true;
                }
            }
        }

        if (item.getItemId() == R.id.action_logout) {
            firebaseAuth.signOut();
            Log.d("MainActivity", "User logged out. Is user still logged in: " + (firebaseAuth.getCurrentUser() != null));
            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // De rol van de gebruiker opzoeken, member of project manager
    private void getUserRole(String userId) {
        FirebaseFirestore.getInstance()
                .collection("users")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                        String role = documentSnapshot.getString("role");
                        if (role != null) {
                            Log.d(TAG, "User id found: " + userId);
                            Log.d(TAG, "User role found: " + role);
                            userRole = role; // Update user role variable
                            invalidateOptionsMenu(); // Refresh menu to reflect changes
                        } else {
                            Log.d(TAG, "Role field not found in user document");
                        }
                    } else {
                        Log.d(TAG, "User document not found for userId: " + userId);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching user document", e);
                });
    }
}