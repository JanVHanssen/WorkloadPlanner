package be.ucll.workloadplanner;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

// Adapter die er voor gaat zorgen dat de tickets mooi in een lijst worden weergegeven
public class TicketsAdapter extends RecyclerView.Adapter<TicketsAdapter.TicketViewHolder> {

    private List<Ticket> ticketList;
    private OnTicketClickListener onTicketClickListener;

    public interface OnTicketClickListener {
        void onTicketClick(Ticket ticket);
    }
    public TicketsAdapter(List<Ticket> ticketList, OnTicketClickListener onTicketClickListener) {
        this.ticketList = ticketList;
        this.onTicketClickListener = onTicketClickListener;
    }

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ticket, parent, false);
        return new TicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        Ticket ticket = ticketList.get(position);
        holder.titleTextView.setText(ticket.getTitle());
        holder.infoTextView.setText(ticket.getInfo());
        holder.finishedTextView.setText(ticket.getFinished() ? "Finished" : "Not Finished");
        User user = ticket.getUser();
        String fullName = user.getFirstname() + " " + user.getLastname();
        holder.userTextView.setText(fullName);
        holder.itemView.setOnClickListener(v -> {
            if (onTicketClickListener != null) {
                onTicketClickListener.onTicketClick(ticket);
            }
        });
    }

    @Override
    public int getItemCount() {
        return ticketList.size();
    }

    static class TicketViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, infoTextView, finishedTextView, userTextView;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            infoTextView = itemView.findViewById(R.id.infoTextView);
            finishedTextView = itemView.findViewById(R.id.finishedTextView);
            userTextView = itemView.findViewById(R.id.userTextView);
        }
    }
}