package com.github.henriquemb.ticketsystem.gui;

import com.github.henriquemb.ticketsystem.TicketSystem;
import com.github.henriquemb.ticketsystem.database.controller.ReportController;
import com.github.henriquemb.ticketsystem.database.model.ReportModel;
import com.github.henriquemb.ticketsystem.enums.ReportStatusEnum;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

/**
 * Нерассмотренные жалобы. ЛКМ — открыть в чате (там же смена статуса), ПКМ — телепорт к нарушителю.
 */
public class ReportsMenu extends ListMenu<ReportModel> {
    public ReportsMenu(Player viewer, int page) {
        super(viewer, "reports", page);
    }

    @Override
    protected List<ReportModel> load() {
        return new ReportController().fetchNotVerified();
    }

    @Override
    protected ItemStack icon(ReportModel report) {
        String status = Arrays.stream(ReportStatusEnum.values())
                .filter(s -> s.getId() == report.getStatus())
                .findFirst()
                .map(ReportStatusEnum::format)
                .orElse("");
        String noReason = TicketSystem.getMessages().getString("report.no_reason");

        Icons icons = new Icons()
                .with("id", report.getId())
                .with("player", report.getPlayer())
                .with("reported", report.getReported())
                .with("status", status)
                .with("date", Icons.formatDate(report.getTimestamp()));

        if (report.getEvidence() != null) icons.text("evidence", report.getEvidence());
        else icons.with("evidence", noReason);

        // Пустая причина подставляется как обычный текст из языкового файла (с его цветами)
        if (report.getReason() != null && !report.getReason().isBlank()) icons.text("text", report.getReason());
        else icons.with("text", noReason);

        return icons.build(TicketSystem.getSettings().icon("report", Material.RED_DYE), "gui.item.report");
    }

    @Override
    protected void click(ReportModel report, ClickType click) {
        runCommand((click.isRightClick() ? "report teleport " : "report view ") + report.getId());
    }
}
