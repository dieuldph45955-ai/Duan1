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

        // Use model helper to get a canonical display code (server code if present, else fallback)
        String displayCode = order.getDisplayCode();
        holder.tvOrderId.setText(String.format(java.util.Locale.getDefault(), "Mã đơn: %s", displayCode));

        String status = order.getStatus();
        if (status == null) status = "";

        // Map raw status to display label (Vietnamese)
        String displayStatus = mapStatusToLabel(status);
        holder.tvStatus.setText(displayStatus);

        // Reset button state
        holder.btnCancel.setVisibility(View.GONE);
        holder.btnCancel.setText("Hủy đơn");
        holder.btnCancel.setOnClickListener(null);

        String lowerStatus = status.toLowerCase().trim();

        if (lowerStatus.contains("pending") || lowerStatus.contains("confirm") || lowerStatus.contains("chờ")) {
            holder.tvStatus.setTextColor(Color.parseColor("#FF9800")); // Cam
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_pending);

            // allow cancelling by owner
            holder.btnCancel.setVisibility(View.VISIBLE);
            holder.btnCancel.setText("Hủy đơn");
            holder.btnCancel.setOnClickListener(v -> {
                if (listener != null) listener.onCancelOrder(order);
            });

        } else if (lowerStatus.contains("delivered") || lowerStatus.contains("giao") || lowerStatus.contains("completed") || lowerStatus.contains("done")) {
            holder.tvStatus.setTextColor(Color.parseColor("#4CAF50")); // Xanh lá
            holder.tvStatus.setBackgroundResource(android.R.color.transparent);

        } else if (lowerStatus.contains("cancel") || lowerStatus.contains("hủy") || lowerStatus.contains("canceled")) {
            holder.tvStatus.setTextColor(Color.parseColor("#F44336")); // Đỏ
            holder.tvStatus.setBackgroundResource(android.R.color.transparent);

            // Show delete for cancelled orders
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
        holder.tvTotal.setText(String.format(java.util.Locale.getDefault(), "%s đ", vn.format(total != null ? total : 0)));

        // Tóm tắt sản phẩm - handle OrderItem which has flexible product type
        StringBuilder summary = new StringBuilder();
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                if (item == null) continue;
                String prodName = item.getProductName();
                long qty = item.getQuantity() != null ? item.getQuantity() : 0L;
                if (prodName == null) prodName = "Sản phẩm";
                summary.append("- ").append(prodName).append(" x").append(qty).append("\n");
            }
        }
        holder.tvItemsSummary.setText(summary.toString().trim());
    }

    @Override
    public int getItemCount() {
        return orderList != null ? orderList.size() : 0;
    }

    private String mapStatusToLabel(String status) {
        if (status == null) return "";
        String s = status.toLowerCase();
        if (s.contains("pending") || s.contains("chờ")) return "Chờ xử lý";
        if (s.contains("confirm")) return "Đã xác nhận";
        if (s.contains("ship") || s.contains("shipping")) return "Đang giao";
        if (s.contains("deliver") || s.contains("delivered") || s.contains("giao")) return "Đã giao";
        if (s.contains("cancel" )|| s.contains("hủy")) return "Đã hủy";
        // else return original but capitalized
        if (!status.isEmpty()) return status.substring(0,1).toUpperCase() + status.substring(1);
        return status;
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
