package be.ucll.workloadplanner;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Fragment om een ticket te updaten voor een project manager
public class UpdateTicketFragment extends Fragment {

    private EditText titleEditText, infoEditText;
    private CheckBox finishedCheckBox;
    private Spinner userSpinner;
    private Button updateButton, deleteButton;
    private List<User> userList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_update_ticket, container, false);

        titleEditText = view.findViewById(R.id.titleEditText);
        infoEditText = view.findViewById(R.id.infoEditText);
        finishedCheckBox = view.findViewById(R.id.finishedCheckBox);
        userSpinner = view.findViewById(R.id.userSpinner);
        updateButton = view.findViewById(R.id.updateButton);
        deleteButton = view.findViewById(R.id.deleteButton);

        retrieveUsersFromDatabase();

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateTicket();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteTicket();
            }
        });

        Bundle args = getArguments();
        if (args != null) {
            String ticketId = args.getString("ticketId");
            String title = args.getString("title");
            String info = args.getString("info");
            boolean finished = args.getBoolean("finished");

            // Set retrieved data to UI fields
            titleEditText.setText(title);
            infoEditText.setText(info);
            finishedCheckBox.setChecked(finished);
        }

        return view;
    }

    // Methode om de gebruiker op te halen uit de database
    private void retrieveUsersFromDatabase() {
        userList = new ArrayList<>();

        FirebaseFirestore.getInstance()
                .collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String firstName = documentSnapshot.getString("firstname");
                        String lastName = documentSnapshot.getString("lastname");

                        User user = new User();
                        user.setFirstname(firstName);
                        user.setLastname(lastName);

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
                    Log.e(TAG, "Error fetching users", e);
                });
    }

    // Methode om de wijzigingen op te slaan in de database
    private void updateTicket() {
        String updatedTitle = titleEditText.getText().toString();
        String updatedInfo = infoEditText.getText().toString();
        boolean updatedFinished = finishedCheckBox.isChecked();

        User selectedUser = userList.get(userSpinner.getSelectedItemPosition());

        Bundle args = getArguments();
        if (args != null) {
            String ticketId = args.getString("ticketId");

            Map<String, Object> ticketData = new HashMap<>();
            ticketData.put("title", updatedTitle);
            ticketData.put("info", updatedInfo);
            ticketData.put("finished", updatedFinished);
            ticketData.put("user", selectedUser);

            FirebaseFirestore.getInstance()
                    .collection("tickets")
                    .document(ticketId)
                    .update(ticketData)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(requireContext(), "Ticket updated successfully!", Toast.LENGTH_SHORT).show();
                            Navigation.findNavController(requireView()).navigate(R.id.action_updateTicket_to_tickets);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "Error updating ticket", e);
                        }
                    });
        }
    }
    // Methode om een ticket te deleten uit de database
    private void deleteTicket() {
        Bundle args = getArguments();
        if (args != null) {
            String ticketId = args.getString("ticketId");

            FirebaseFirestore.getInstance()
                    .collection("tickets")
                    .document(ticketId)
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(requireContext(), "Ticket deleted successfully!", Toast.LENGTH_SHORT).show();
                            Navigation.findNavController(requireView()).navigate(R.id.action_updateTicket_to_tickets);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "Error deleting ticket", e);
                        }
                    });
        }
    }
}