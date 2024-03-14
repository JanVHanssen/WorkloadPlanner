package be.ucll.workloadplanner;

import static android.app.PendingIntent.getActivity;
import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

// Scherm waarin je een overzicht ziet van de tickets
public class TicketsFragment extends Fragment implements TicketsAdapter.OnTicketClickListener {

    private RecyclerView recyclerView;
    private List<Ticket> ticketList;
    private TicketsAdapter ticketsAdapter;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tickets, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        ticketList = new ArrayList<>();

        ticketsAdapter = new TicketsAdapter(ticketList, this);
        recyclerView.setAdapter(ticketsAdapter);

        retrieveTicketsFromFirestore();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView.setAdapter(ticketsAdapter);
    }

    // De lijst met tickets ophalen uit de database
    private void retrieveTicketsFromFirestore() {
        db.collection("tickets")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ticketList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Ticket ticket = document.toObject(Ticket.class);
                            ticketList.add(ticket);
                        }
                        ticketsAdapter.notifyDataSetChanged();
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }

    // Wanneer er op een ticket geklikt wordt
    @Override
    public void onTicketClick(Ticket ticket) {
        FirebaseUser currentUser = getCurrentUser();
        if (currentUser != null) {
            String phoneNumber = currentUser.getPhoneNumber();
            if (phoneNumber != null) {
                getUserFromFirestoreAndProceed(phoneNumber, ticket);
            } else {
                Log.e(TAG, "User's phone number is null");
            }
        } else {
            Log.e(TAG, "Current user is null");
        }
    }

    // Huidige gebruiker ophalen
    private FirebaseUser getCurrentUser() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        return auth.getCurrentUser();
    }

    // Ophalen van het juiste info gekoppeld aan de gebruiker
    private void getUserFromFirestoreAndProceed(String phoneNumber, Ticket ticket) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").whereEqualTo("userId", phoneNumber)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            User user = document.toObject(User.class);
                            String userId = user.getUserId();
                            getUserRole(userId, ticket);
                            break;
                        }
                    } else {
                        Log.e(TAG, "No user found with the given phone number: " + phoneNumber);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch user data from Firestore: " + e.getMessage());
                });
    }

    // Ophalen van de rol van de huidige gebruiker, member of project manager
    private void getUserRole(String userId, Ticket ticket) {
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
                            navigateToUpdateFragmentOrRestrictedFragment(role, ticket);
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

    // Gebruiker doorsturen naar het juiste fragment op basis van hun rol
    private void navigateToUpdateFragmentOrRestrictedFragment(String userRole, Ticket ticket) {
        if (userRole.equals("Project Manager")) {
            Bundle bundle = new Bundle();
            bundle.putString("ticketId", ticket.getTicketId());
            bundle.putString("title", ticket.getTitle());
            bundle.putString("info", ticket.getInfo());
            bundle.putBoolean("finished", ticket.getFinished());
            bundle.putString("firstName", ticket.getUser().getFirstname());
            bundle.putString("lastName", ticket.getUser().getLastname());
            Navigation.findNavController(requireView()).navigate(R.id.action_tickets_to_updateTicket, bundle);
        } else if (userRole.equals("Member")) {
            Bundle bundle = new Bundle();
            bundle.putString("ticketId", ticket.getTicketId());
            bundle.putString("title", ticket.getTitle());
            bundle.putString("info", ticket.getInfo());
            bundle.putBoolean("finished", ticket.getFinished());
            bundle.putString("firstName", ticket.getUser().getFirstname());
            bundle.putString("lastName", ticket.getUser().getLastname());
            Navigation.findNavController(requireView()).navigate(R.id.action_tickets_to_updateTicketRestricted, bundle);
        }
    }
}