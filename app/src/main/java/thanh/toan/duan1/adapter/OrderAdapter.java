package thanh.toan.duan1.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import thanh.toan.duan1.R;
import thanh.toan.duan1.model.OrderItem;
import thanh.toan.duan1.model.Order;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    public interface OnOrderActionListener {
        void onCancelOrder(Order order);
        void onDeleteOrder(Order order);
    }

    private Context context;
    private List<Order> orderList;
    private OnOrderActionListener listener;

    public OrderAdapter(Context context, List<Order> orderList, OnOrderActionListener listener) {
        this.context = context;
        this.orderList = orderList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        String orderId = order.getID() != null && order.getID().length() >= 8 
                ? order.getID().substring(0, 8).toUpperCase() 
                : order.getID();
        holder.tvOrderId.setText("Mã đơn: " + orderId);
        
        String status = order.getStatus();
        if (status == null) status = "";
        
        holder.tvStatus.setText(status);
        
        // Reset trạng thái nút
        holder.btnCancel.setVisibility(View.GONE);
        holder.btnCancel.setText("Hủy đơn");
        holder.btnCancel.setOnClickListener(null);

        String lowerStatus = status.toLowerCase().trim();

        if (lowerStatus.equals("pending") || lowerStatus.equals("chờ xác nhận")) {
            holder.tvStatus.setTextColor(Color.parseColor("#FF9800")); // Cam
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_pending);
            
            holder.btnCancel.setVisibility(View.VISIBLE);
            holder.btnCancel.setText("Hủy đơn");
            holder.btnCancel.setOnClickListener(v -> {
                if (listener != null) listener.onCancelOrder(order);
            });

        } else if (lowerStatus.equals("completed") || lowerStatus.equals("giao hàng thành công")) {
            holder.tvStatus.setTextColor(Color.parseColor("#4CAF50")); // Xanh lá
            holder.tvStatus.setBackgroundResource(android.R.color.transparent);
            
        } else if (lowerStatus.equals("cancelled") || lowerStatus.equals("canceled") || lowerStatus.equals("đã hủy")) {
            holder.tvStatus.setTextColor(Color.parseColor("#F44336")); // Đỏ
            holder.tvStatus.setBackgroundResource(android.R.color.transparent);
            
            // Hiển thị nút Xóa cho đơn đã hủy
            holder.btnCancel.setVisibility(View.VISIBLE);
            holder.btnCancel.setText("Xóa đơn");
            holder.btnCancel.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteOrder(order);
            });

        } else {
            holder.tvStatus.setTextColor(Color.parseColor("#757575")); // Xám
            holder.tvStatus.setBackgroundResource(android.R.color.transparent);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        if (order.getCreatedAt() != null) {
            holder.tvDate.setText(sdf.format(order.getCreatedAt()));
        } else {
            holder.tvDate.setText("N/A");
        }

        // Tổng tiền
        NumberFormat vn = NumberFormat.getInstance(new Locale("vi", "VN"));
        Long total = order.getTotalPrice();
        holder.tvTotal.setText(vn.format(total != null ? total : 0) + " đ");

        // Tóm tắt sản phẩm
        StringBuilder summary = new StringBuilder();
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                summary.append("- ").append(item.getProductName())
                       .append(" x").append(item.getQuantity()).append("\n");
            }
        }
        holder.tvItemsSummary.setText(summary.toString().trim());
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvStatus, tvDate, tvItemsSummary, tvTotal;
        Button btnCancel;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.order_id);
            tvStatus = itemView.findViewById(R.id.order_status);
            tvDate = itemView.findViewById(R.id.order_date);
            tvItemsSummary = itemView.findViewById(R.id.order_items_summary);
            tvTotal = itemView.findViewById(R.id.order_total);
            btnCancel = itemView.findViewById(R.id.btn_cancel_order);
        }
    }
}
