package com.DMWatch;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.FriendsChatRank;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.clan.ClanTitle;
import net.runelite.client.game.ChatIconManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.Text;

@Singleton
public class PartyMemberTierOverlay extends Overlay
{
	private static final int ACTOR_OVERHEAD_TEXT_MARGIN = 40;
	private static final int ACTOR_HORIZONTAL_TEXT_MARGIN = 10;

	private final PartyMemberIndicatorService playerIndicatorsService;
	private final DMWatchConfig config;

	@Inject
	private PartyMemberTierOverlay(DMWatchConfig config, PartyMemberIndicatorService playerIndicatorsService)
	{
		this.config = config;
		this.playerIndicatorsService = playerIndicatorsService;
		setPosition(OverlayPosition.DYNAMIC);
		setPriority(OverlayPriority.MED);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		playerIndicatorsService.forEachPlayer((player, decorations) -> renderPlayerOverlay(graphics, player, decorations));
		return null;
	}

	private void renderPlayerOverlay(Graphics2D graphics, Player actor, PartyMemberIndicatorService.Decorations decorations)
	{
		final PlayerNameLocation drawPlayerNamesConfig = config.playerNamePosition();
		if (drawPlayerNamesConfig == PlayerNameLocation.DISABLED)
		{
			return;
		}

		final int zOffset;
		switch (drawPlayerNamesConfig)
		{
			case MODEL_CENTER:
			case MODEL_RIGHT:
				zOffset = actor.getLogicalHeight() / 2;
				break;
			default:
				zOffset = actor.getLogicalHeight() + ACTOR_OVERHEAD_TEXT_MARGIN;
		}

		final String name = Text.sanitize(actor.getName());
		Point textLocation = actor.getCanvasTextLocation(graphics, name, zOffset);

		if (drawPlayerNamesConfig == PlayerNameLocation.MODEL_RIGHT)
		{
			textLocation = actor.getCanvasTextLocation(graphics, "", zOffset);

			if (textLocation == null)
			{
				return;
			}

			textLocation = new Point(textLocation.getX() + ACTOR_HORIZONTAL_TEXT_MARGIN, textLocation.getY());
		}

		if (textLocation == null)
		{
			return;
		}

		BufferedImage scammerImage = decorations.getScammerIcon();

		if (scammerImage != null)
		{
			final int imageWidth = scammerImage.getWidth();
			final int imageTextMargin;
			final int imageNegativeMargin;

			if (drawPlayerNamesConfig == PlayerNameLocation.MODEL_RIGHT)
			{
				imageTextMargin = imageWidth;
				imageNegativeMargin = 0;
			}
			else
			{
				imageTextMargin = imageWidth / 2;
				imageNegativeMargin = imageWidth / 2;
			}

			final int textHeight = graphics.getFontMetrics().getHeight() - graphics.getFontMetrics().getMaxDescent();

			final Point imageLocation = new Point(textLocation.getX() - imageNegativeMargin - 8, textLocation.getY() - textHeight / 2 - scammerImage.getHeight() / 2);
			OverlayUtil.renderImageLocation(graphics, imageLocation, scammerImage);
			textLocation = new Point(textLocation.getX() - 5 + imageTextMargin, textLocation.getY());
		}

		OverlayUtil.renderTextLocation(graphics, textLocation, name, decorations.getColor());
	}
}