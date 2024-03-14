package be.ucll.workloadplanner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

// Scherm waarin de gebruiker een ticket kan toevoegen
public class AddTicketFragment extends Fragment {

    private EditText titleEditText, infoEditText;
    private Button addButton;
    private Spinner userSpinner;
    private FirebaseFirestore db;
    private List<User> userList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_ticket, container, false);

        db = FirebaseFirestore.getInstance();

        titleEditText = view.findViewById(R.id.title);
        infoEditText = view.findViewById(R.id.info);
        addButton = view.findViewById(R.id.addTicket);
        userSpinner = view.findViewById(R.id.user);

        loadUsersFromDatabase();

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTicket();
            }
        });

        return view;
    }

    // Ophalen van de lijst met gebruikers uit de database
    private void loadUsersFromDatabase() {
        userList = new ArrayList<>();

        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        User user = documentSnapshot.toObject(User.class);
                        userList.add(user);
                    }

                    // De 'nobody' optie toevoegen
                    User nobodyUser = new User();
                    nobodyUser.setUserId("none");
                    nobodyUser.setFirstname("Nobody");
                    nobodyUser.setLastname("Unassigned");
                    userList.add(0, nobodyUser);

                    List<String> userNames = new ArrayList<>();
                    for (User user : userList) {
                        userNames.add(user.getFirstname() + " " + user.getLastname());
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, userNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    userSpinner.setAdapter(adapter);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load users", Toast.LENGTH_SHORT).show();
                });
    }

    // Methode voor een nieuwe ticket toe te voegen
    private void addTicket() {
        String title = titleEditText.getText().toString().trim();
        String info = infoEditText.getText().toString().trim();
        boolean finished = false;
        User selectedUser = null;

        if (title.isEmpty() || info.isEmpty()) {
            Toast.makeText(getContext(), "Title or info cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        String selectedUserName = userSpinner.getSelectedItem().toString();

        for (User user : userList) {
            if ((user.getFirstname() + " " + user.getLastname()).equals(selectedUserName)) {
                selectedUser = user;
                break;
            }
        }

        if (selectedUser != null && selectedUser.getUserId() != null) {
            Ticket ticket = new Ticket(null, title, info, finished, selectedUser);

            db.collection("tickets")
                    .add(ticket)
                    .addOnSuccessListener(documentReference -> {
                        String generatedTicketId = documentReference.getId();
                        ticket.setTicketId(generatedTicketId);
                        db.collection("tickets").document(generatedTicketId)
                                .set(ticket)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(getContext(), "Ticket added successfully", Toast.LENGTH_SHORT).show();
                                    NavHostFragment.findNavController(AddTicketFragment.this)
                                            .navigate(R.id.action_addTicket_to_tickets);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "Failed to add ticket", Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to add ticket", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(getContext(), "Selected user or user ID is null", Toast.LENGTH_SHORT).show();
        }
    }
}