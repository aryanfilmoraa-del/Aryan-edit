package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = CosmicDark
        ) {
          PortfolioApp()
        }
      }
    }
  }
}

// Data Classes
data class SoftwareTool(
  val name: String,
  val type: String,
  val icon: ImageVector,
  val glowingColor: Color,
  val techSpec: String,
  val useCase: String
)

data class AiTool(
  val name: String,
  val capability: String,
  val purpose: String,
  val scale: String, // e.g. "92% Efficiency Rating"
  val glowingColor: Color
)

data class ServiceItem(
  val category: String, // Short Form, Long Form, Professional Features
  val title: String,
  val description: String,
  val subItems: List<String>,
  val accentColor: Color
)

data class PortfolioVideo(
  val title: String,
  val category: String,
  val duration: String,
  val views: String,
  val date: String,
  val description: String,
  val videoUrlMock: String,
  val isVertical: Boolean = false,
  val timelineTracks: List<String> = listOf("Video 1", "B-Roll 1.mp4", "Audio SFX", "Background Music"),
  val colorGradingBefore: Color = Color(0xFFAAAAAA),
  val colorGradingAfter: Color = Color(0xFF6B21A8)
)

data class ClientReview(
  val reviewerName: String,
  val role: String,
  val rating: Int,
  val reviewText: String,
  val date: String,
  val isVerified: Boolean = true
)

// Main Portfolio Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortfolioApp() {
  val scrollState = rememberScrollState()
  val scope = rememberCoroutineScope()
  val context = LocalContext.current
  val uriHandler = LocalUriHandler.current

  // Anchored Scroll Coordinates State (Approximate heights or targeted scroll indices)
  // To keep navigation responsive, we track the current section and offer an interactive side bar / top bar
  var activeTab by remember { mutableStateOf("Home") }

  // Dynamic Portfolio Category Filter
  var activePortfolioFilter by remember { mutableStateOf("All") }

  // Video Preview Modal State
  var selectedVideoForModal by remember { mutableStateOf<PortfolioVideo?>(null) }

  // Custom Pricing Calculator State
  var calcVideoType by remember { mutableStateOf("Reel / Short") } // Reel / Short, Long Form, Corporate
  var calcDurationMins by remember { mutableStateOf(3f) } // 1 - 30 mins
  var calcAddGrading by remember { mutableStateOf(true) }
  var calcAddSubtitles by remember { mutableStateOf(true) }
  var calcAddSFX by remember { mutableStateOf(true) }
  var calcAddVFX by remember { mutableStateOf(false) }

  // Review List State with custom local additions (Persistence simulation)
  val reviewsList = remember {
    mutableStateListOf(
      ClientReview("Nathaniel Hall", "YouTube Creator (1.2M Subs)", 5, "Outstanding editing quality and fast delivery. Aryan has completely revolutionized my retention stats! High-end animations are superb.", "June 2, 2026"),
      ClientReview("Sarah Jenkins", "Brand Director at Lumos Co", 5, "Highly recommended for professional content creators. The color grading on our ads made them look like TV commercials. Fast turnaround too.", "May 28, 2026"),
      ClientReview("Alex Rivera", "Professional Adventurer & Vlogger", 5, "Excellent communication and amazing creativity. Aryan's travel vlogs editing capture the soul! Transitions are incredibly smooth.", "May 15, 2026")
    )
  }

  // Submit Review Modal State
  var showAddReviewDialog by remember { mutableStateOf(false) }

  // Contact Form Inputs
  var contactName by remember { mutableStateOf("") }
  var contactEmail by remember { mutableStateOf("") }
  var contactPhone by remember { mutableStateOf("") }
  var contactProjectType by remember { mutableStateOf("Reel / Short Editing") }
  val projectTypes = listOf("Reel / Short Editing", "YouTube Video", "Gaming Montage", "Cinematic travel edit", "Business Ad", "Motion Graphics")
  var contactBudget by remember { mutableStateOf("₹1,000 - ₹5,000") }
  val budgetsOptions = listOf("Under ₹2,000", "₹2,000 - ₹5,000", "₹5,000 - ₹10,000", "₹10,000 - ₹25,000", "Custom (Enterprise)")
  var contactMessage by remember { mutableStateOf("") }
  var showFeedbackDialog by remember { mutableStateOf(false) }
  var showFeedbackError by remember { mutableStateOf(false) }

  // Dynamic Canvas Particle Effect
  var canvasTicks by remember { mutableStateOf(0f) }
  LaunchedEffect(Unit) {
    while (true) {
      withFrameMillis {
        canvasTicks += 0.05f
      }
    }
  }

  // Gradient definitions
  val purplePinkGrad = Brush.horizontalGradient(colors = listOf(ElectricPurple, VividPink))
  val cyanBlueGrad = Brush.horizontalGradient(colors = listOf(CyanBlue, ElectricBlue))
  val backgroundFade = Brush.verticalGradient(colors = listOf(CosmicDark, CosmicSlate, CosmicDark))

  // App Structure Header & Content
  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable {
              scope.launch { scrollState.animateScrollTo(0) }
              activeTab = "Home"
            }
          ) {
            Box(
              modifier = Modifier
                .size(34.dp)
                .background(
                  brush = Brush.radialGradient(listOf(ElectricPurple, Color.Transparent)),
                  shape = CircleShape
                )
                .border(1.dp, CyanBlue, CircleShape)
                .padding(4.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "AE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = CyanBlue,
                letterSpacing = (-1).sp
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "Aryan Editor",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = GlowSilver
              )
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(6.dp)
                    .background(CyanBlue, CircleShape)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "uxaryanedit",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Medium,
                  color = CyanBlue
                )
              }
            }
          }
        },
        actions = {
          // Custom Quick Nav tabs on App Bar
          IconButton(
            onClick = {
              scope.launch { scrollState.animateScrollTo(1400) } // Rough position of Software/AI tool
              activeTab = "Tools"
            }
          ) {
            Icon(Icons.Default.Build, contentDescription = "Tools", tint = if (activeTab == "Tools") CyanBlue else GlowSilver.copy(alpha = 0.7f))
          }
          IconButton(
            onClick = {
              scope.launch { scrollState.animateScrollTo(2300) } // Rough position of Services
              activeTab = "Services"
            }
          ) {
            Icon(Icons.Default.List, contentDescription = "Services", tint = if (activeTab == "Services") CyanBlue else GlowSilver.copy(alpha = 0.7f))
          }
          IconButton(
            onClick = {
              scope.launch { scrollState.animateScrollTo(3650) } // Rough position of Portfolio
              activeTab = "Portfolio"
            }
          ) {
            Icon(Icons.Default.PlayArrow, contentDescription = "Portfolio", tint = if (activeTab == "Portfolio") CyanBlue else GlowSilver.copy(alpha = 0.7f))
          }
          IconButton(
            onClick = {
              scope.launch { scrollState.animateScrollTo(5400) } // Rough position of Pricing
              activeTab = "Pricing"
            }
          ) {
            Icon(Icons.Default.Star, contentDescription = "Pricing", tint = if (activeTab == "Pricing") CyanBlue else GlowSilver.copy(alpha = 0.7f))
          }
          Button(
            onClick = {
              scope.launch { scrollState.animateScrollTo(8000) } // Bottom Contact
              activeTab = "Contact"
            },
            colors = ButtonDefaults.buttonColors(containerColor = CosmicCard),
            border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(ElectricPurple, ElectricBlue))),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
            modifier = Modifier
              .padding(end = 8.dp)
              .height(34.dp)
          ) {
            Text("Hire Me", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GlowSilver)
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = CosmicSlate.copy(alpha = 0.95f),
          scrolledContainerColor = CosmicDark
        ),
        modifier = Modifier.statusBarsPadding()
      )
    },
    bottomBar = {
      // Dynamic footer panel indicating status
      BottomAppBar(
        containerColor = CosmicSlate,
        contentPadding = PaddingValues(horizontal = 16.dp),
        modifier = Modifier.height(48.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .background(Color(0xFF00FF66), CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Available for Freelance Projects",
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              color = Color(0xFF00FF66)
            )
          }
          Text(
            text = "Aryan Editor © 2026",
            fontSize = 10.sp,
            color = GlowSilver.copy(alpha = 0.5f)
          )
        }
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(backgroundFade)
        .padding(innerPadding)
        .drawBehind {
          // Draw subtle background glowing waves or particles to look highly cinematic
          drawCircle(
            color = ElectricPurple.copy(alpha = 0.15f),
            radius = 350.dp.toPx(),
            center = Offset(size.width * 0.1f, size.height * 0.15f)
          )
          drawCircle(
            color = ElectricBlue.copy(alpha = 0.12f),
            radius = 300.dp.toPx(),
            center = Offset(size.width * 0.8f, size.height * 0.45f)
          )
          drawCircle(
            color = VividPink.copy(alpha = 0.1f),
            radius = 280.dp.toPx(),
            center = Offset(size.width * 0.3f, size.height * 0.75f)
          )

          // Subdued cinematic grid lines
          val step = 80.dp.toPx()
          var x = 0f
          while (x < size.width) {
            drawLine(
              color = Color.White.copy(alpha = 0.015f),
              start = Offset(x, 0f),
              end = Offset(x, size.height),
              strokeWidth = 1f
            )
            x += step
          }
          var y = 0f
          while (y < size.height) {
            drawLine(
              color = Color.White.copy(alpha = 0.015f),
              start = Offset(0f, y),
              end = Offset(size.width, y),
              strokeWidth = 1f
            )
            y += step
          }
        }
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(scrollState)
      ) {

        // --- HERO SECTION ---
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
          Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            // High-end glowing Creator Tag
            Box(
              modifier = Modifier
                .background(ElectricPurple.copy(alpha = 0.15f), RoundedCornerShape(100.dp))
                .border(
                  BorderStroke(1.dp, Brush.horizontalGradient(listOf(ElectricPurple, CyanBlue))),
                  RoundedCornerShape(100.dp)
                )
                .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = "Verified Editing",
                  tint = CyanBlue,
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "PREMIUM CINEMATIC STORIES",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Black,
                  letterSpacing = 1.5.sp,
                  color = GlowSilver
                )
              }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Main Cinematic Headline
            Text(
              text = "Transforming Raw Footage\nInto Engaging Stories",
              fontSize = 32.sp,
              lineHeight = 40.sp,
              fontWeight = FontWeight.Black,
              textAlign = TextAlign.Center,
              style = TextStyle(
                brush = Brush.verticalGradient(
                  colors = listOf(GlowSilver, GlowSilver, ElectricPurple)
                )
              ),
              modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Professional Subheading
            Text(
              text = "I help creators, brands, businesses, and influencers grow through professional video editing, cinematic storytelling, social media content, reels, shorts, and AI-powered editing.",
              fontSize = 14.sp,
              lineHeight = 22.sp,
              textAlign = TextAlign.Center,
              color = TextMuted,
              modifier = Modifier
                .padding(horizontal = 12.dp)
                .alpha(0.85f)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Action CTAs Button row
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.Center,
              verticalAlignment = Alignment.CenterVertically
            ) {
              // Primary Call to Action
              Button(
                onClick = {
                  scope.launch { scrollState.animateScrollTo(8000) } // Go to Contact Now
                  activeTab = "Contact"
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
                modifier = Modifier
                  .height(50.dp)
                  .weight(1f)
                  .padding(end = 8.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
              ) {
                Icon(Icons.Default.Send, contentDescription = "Hire", tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Hire Aryan", fontWeight = FontWeight.Bold, fontSize = 14.sp)
              }

              // Secondary Call to Action
              OutlinedButton(
                onClick = {
                  scope.launch { scrollState.animateScrollTo(3650) } // Go to Portfolio
                  activeTab = "Portfolio"
                },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.5.dp, ElectricBlue),
                modifier = Modifier
                  .height(50.dp)
                  .weight(1f)
                  .padding(start = 8.dp)
              ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Videos", tint = ElectricBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text("View Portfolio", color = GlowSilver, fontWeight = FontWeight.Bold, fontSize = 14.sp)
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Stats Bar
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .background(CosmicCard.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                .padding(vertical = 12.dp),
              horizontalArrangement = Arrangement.SpaceEvenly,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("150+", fontSize = 20.sp, fontWeight = FontWeight.Black, color = CyanBlue)
                Text("Videos Edited", fontSize = 10.sp, color = TextMuted)
              }
              Box(modifier = Modifier.size(1.dp, 30.dp).background(Color.White.copy(alpha = 0.2f)))
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("40M+", fontSize = 20.sp, fontWeight = FontWeight.Black, color = VividPink)
                Text("Total Views Generated", fontSize = 10.sp, color = TextMuted)
              }
              Box(modifier = Modifier.size(1.dp, 30.dp).background(Color.White.copy(alpha = 0.2f)))
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("100%", fontSize = 20.sp, fontWeight = FontWeight.Black, color = ElectricBlue)
                Text("On-Time Delivery", fontSize = 10.sp, color = TextMuted)
              }
            }
          }
        }


        // --- ABOUT ME SECTION ---
        SectionHeading(title = "About Aryan", subtitle = "The story Behind the Lens")

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
        ) {
          Column(
            modifier = Modifier
              .background(CosmicCard.copy(alpha = 0.8f), RoundedCornerShape(24.dp))
              .border(
                BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                RoundedCornerShape(24.dp)
              )
              .padding(20.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(bottom = 16.dp)
            ) {
              // Virtual Cinematic Avatar Placeholder
              Box(
                modifier = Modifier
                  .size(64.dp)
                  .background(ElectricPurple.copy(alpha = 0.2f), CircleShape)
                  .border(2.dp, purplePinkGrad, CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Person,
                  contentDescription = "Aryan Profile",
                  tint = ElectricPurple,
                  modifier = Modifier.size(36.dp)
                )
              }

              Spacer(modifier = Modifier.width(16.dp))

              Column {
                Text(
                  text = "Aryan",
                  fontSize = 22.sp,
                  fontWeight = FontWeight.Bold,
                  color = GlowSilver
                )
                Text(
                  text = "Professional Video Editor • Retainer Specialist",
                  fontSize = 12.sp,
                  color = CyanBlue,
                  fontWeight = FontWeight.SemiBold
                )
              }
            }

            Text(
              text = "I am a passionate and creative video editor with experience in editing social media content, YouTube videos, reels, shorts, promotional videos, gaming videos, cinematic edits, travel videos, business advertisements, and personal branding content.",
              fontSize = 14.sp,
              lineHeight = 22.sp,
              color = TextMuted,
              modifier = Modifier.padding(bottom = 20.dp)
            )

            Text(
              text = "My Main Objective Focuses:",
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = GlowSilver,
              modifier = Modifier.padding(bottom = 10.dp)
            )

            // Bullet Grid
            val bulletPoints = listOf(
              "High-quality editing" to Icons.Default.CheckCircle,
              "Fast delivery" to Icons.Default.Send,
              "Audience retention" to Icons.Default.PlayArrow,
              "Viral editing techniques" to Icons.Default.Notifications,
              "Professional color grading" to Icons.Default.Star,
              "Smooth transitions" to Icons.Default.Share,
              "Motion graphics" to Icons.Default.Build,
              "Sound design" to Icons.Default.Favorite,
              "AI-powered content enhancement" to Icons.Default.Add
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              bulletPoints.chunked(2).forEach { pairList ->
                Row(modifier = Modifier.fillMaxWidth()) {
                  pairList.forEach { point ->
                    Row(
                      modifier = Modifier
                        .weight(1f)
                        .padding(end = 4.dp),
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Icon(
                        imageVector = point.second,
                        contentDescription = "Bullet Point",
                        tint = ElectricBlue,
                        modifier = Modifier.size(16.dp)
                      )
                      Spacer(modifier = Modifier.width(8.dp))
                      Text(
                        text = point.first,
                        fontSize = 12.sp,
                        color = GlowSilver.copy(alpha = 0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                      )
                    }
                  }
                  // Fill gap if odd number
                  if (pairList.size < 2) {
                    Spacer(modifier = Modifier.weight(1f))
                  }
                }
              }
            }
          }
        }


        // --- SKILLS SECTION WITH PROGRESS METERS ---
        SectionHeading(title = "Craft Proficiency", subtitle = "Skill breakdown & experience index")

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
        ) {
          Column(
            modifier = Modifier
              .background(CosmicCard.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
              .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
              .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
          ) {
            val skillMeters = listOf(
              "Video Editing" to (0.95f to CyanBlue),
              "Color Grading" to (0.90f to ElectricPurple),
              "Motion Graphics" to (0.85f to VividPink),
              "Sound Design" to (0.88f to ElectricBlue),
              "AI Content Enhancement" to (0.92f to CyanBlue),
              "Social Media Content" to (0.95f to ElectricPurple)
            )

            skillMeters.forEach { (name, pairValue) ->
              val (percentValue, color) = pairValue
              SkillProgressBar(name = name, percent = percentValue, progressColor = color)
            }
          }
        }


        // --- EDITING SOFTWARE I USE ---
        SectionHeading(
          title = "Core Editing Suite",
          subtitle = "I use industry-standard editing software to create professional, high-quality content for every platform."
        )

        val softwareSuite = listOf(
          SoftwareTool("DaVinci Resolve", "Color & Assembly", Icons.Default.Star, CyanBlue, "10-bit Raw Integration", "Hollywood-grade color correction & grading"),
          SoftwareTool("After Effects", "VFX & Motion Graphics", Icons.Default.Build, ElectricPurple, "Dynamic Vector Engine", "Cinematic intros, custom assets & text animation"),
          SoftwareTool("CapCut Pro", "Fast Paced Social", Icons.Default.PlayArrow, VividPink, "Fast Metadata Export", "Trending reels, automated sound templates & social posts"),
          SoftwareTool("KineMaster", "Mobile Workflow", Icons.Default.Phone, ElectricBlue, "On-the-go timeline", "Mobile editing and quick client draft review previews"),
          SoftwareTool("InShot", "Social Formatting", Icons.Default.Refresh, CyanBlue, "Responsive canvases", "Perfect aspects for IG story, Tiktok drafts & layouts"),
          SoftwareTool("Premiere Pro", "Comprehensive Timeline", Icons.Default.List, ElectricPurple, "High Capacity Encoding", "Long format documentary and podcast sequence assembly"),
          SoftwareTool("Photoshop", "Thumbnail Crafting", Icons.Default.Create, VividPink, "Layer Processing", "High click-through-rate YouTube visual packaging"),
          SoftwareTool("Lightroom", "Color Theory Lab", Icons.Default.Home, ElectricBlue, "Raw profiles", "Precise exposure balancing, lookup-tables generation")
        )

        LazyRow(
          contentPadding = PaddingValues(horizontal = 16.dp),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          items(softwareSuite) { tool ->
            var activeCardDetail by remember { mutableStateOf(false) }

            Box(
              modifier = Modifier
                .width(180.dp)
                .height(185.dp)
                .background(
                  color = if (activeCardDetail) CosmicSlate else CosmicCard.copy(alpha = 0.8f),
                  shape = RoundedCornerShape(20.dp)
                )
                .border(
                  BorderStroke(
                    1.2.dp,
                    if (activeCardDetail) tool.glowingColor else Color.White.copy(alpha = 0.08f)
                  ),
                  RoundedCornerShape(20.dp)
                )
                .clickable { activeCardDetail = !activeCardDetail }
                .padding(16.dp)
            ) {
              Column(modifier = Modifier.fillMaxSize()) {
                Box(
                  modifier = Modifier
                    .size(40.dp)
                    .background(tool.glowingColor.copy(alpha = 0.15f), CircleShape)
                    .border(1.dp, tool.glowingColor.copy(alpha = 0.4f), CircleShape),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = tool.icon,
                    contentDescription = tool.name,
                    tint = tool.glowingColor,
                    modifier = Modifier.size(20.dp)
                  )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                  text = tool.name,
                  fontSize = 15.sp,
                  fontWeight = FontWeight.Bold,
                  color = GlowSilver
                )

                Text(
                  text = tool.type,
                  fontSize = 11.sp,
                  color = tool.glowingColor,
                  fontWeight = FontWeight.SemiBold,
                  modifier = Modifier.padding(bottom = 6.dp)
                )

                if (activeCardDetail) {
                  Text(
                    text = tool.useCase,
                    fontSize = 10.sp,
                    color = TextMuted,
                    lineHeight = 13.sp,
                    maxLines = 3
                  )
                } else {
                  Text(
                    text = "SPEC: ${tool.techSpec}",
                    fontSize = 10.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Medium
                  )
                  Spacer(modifier = Modifier.weight(1f))
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.alpha(0.6f)
                  ) {
                    Text("Tap for details", fontSize = 9.sp, color = TextMuted)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                      imageVector = Icons.Default.Info,
                      contentDescription = "Details",
                      tint = TextMuted,
                      modifier = Modifier.size(10.dp)
                    )
                  }
                }
              }
            }
          }
        }


        // --- AI TOOLS I USE ---
        SectionHeading(
          title = "Modern AI Workflow",
          subtitle = "I combine professional editing skills with modern AI tools to deliver faster, smarter, and more engaging content."
        )

        val aiToolsList = listOf(
          AiTool("ChatGPT", "Prompt & Hook Architecture", "Drafting attention retaining hooks & dialogue adjustments", "96% Efficiency", CyanBlue),
          AiTool("Google Gemini", "Fast Metadata & SEO Tags", "Constructing descriptions that algorithmically match views", "95% Optimization", ElectricPurple),
          AiTool("Claude AI", "Script Analysis & Flow", "Analyzing screenplay pacing for documentary assemblies", "92% flow index", ElectricBlue),
          AiTool("Midjourney", "Digital Asset Creation", "Generating seamless overlay backgrounds & retro static assets", "Infinite outputs", VividPink),
          AiTool("Runway ML", "AI Inpainting & Rotoscoping", "Instant object elimination and smart mask creation", "10x speed boost", CyanBlue),
          AiTool("Pika Labs", "Text To Video Motion", "Fleshing short dynamic custom elements from static icons", "Cinematic density", ElectricPurple),
          AiTool("ElevenLabs", "AI Voice Processing & SFX", "Generating hyper-real atmospheric voiceovers & clean acoustics", "99% fidelity", ElectricBlue),
          AiTool("Adobe Firefly", "Vector Assets & Backdrop", "Generative extensions for landscape orientation edits", "High resolution", VividPink),
          AiTool("Canva AI", "Smart Image Up-scaling", "Enhancing resolution and creating template guidelines", "Loss-less scaling", CyanBlue),
          AiTool("Leonardo AI", "Concept Graphic Layouts", "Rendering highly stylistic thumbnail vectors", "Custom styled", ElectricPurple)
        )

        Column(
          modifier = Modifier.padding(horizontal = 16.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          aiToolsList.chunked(2).forEach { rowList ->
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              rowList.forEach { ai ->
                Box(
                  modifier = Modifier
                    .weight(1f)
                    .background(CosmicCard.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                    .border(
                      BorderStroke(1.dp, Color.White.copy(alpha = 0.04f)),
                      RoundedCornerShape(16.dp)
                    )
                    .padding(12.dp)
                ) {
                  Column {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.SpaceBetween,
                      modifier = Modifier.fillMaxWidth()
                    ) {
                      Text(
                        text = ai.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlowSilver
                      )
                      Box(
                        modifier = Modifier
                          .background(ai.glowingColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                          .border(0.5.dp, ai.glowingColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                          .padding(horizontal = 6.dp, vertical = 2.dp)
                      ) {
                        Text(
                          text = ai.scale,
                          fontSize = 8.sp,
                          color = ai.glowingColor,
                          fontWeight = FontWeight.Black
                        )
                      }
                    }
                    Text(
                      text = ai.capability,
                      fontSize = 11.sp,
                      color = ai.glowingColor,
                      fontWeight = FontWeight.Medium,
                      modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                    )
                    Text(
                      text = ai.purpose,
                      fontSize = 10.sp,
                      color = TextMuted,
                      lineHeight = 13.sp,
                      maxLines = 2
                    )
                  }
                }
              }
              if (rowList.size < 2) {
                Spacer(modifier = Modifier.weight(1f))
              }
            }
          }
        }


        // --- SERVICES SECTION ---
        SectionHeading(title = "Professional Services", subtitle = "Premium High-converting Solutions")

        val servicesList = listOf(
          ServiceItem(
            "Short Form Content",
            "Micro-Attention Reel Mastery",
            "Tailored for Instagram Reels, YouTube Shorts, TikToks, and Facebook Reels. Built to disrupt continuous-scrolling behaviors.",
            listOf("Audience Disruptive Hooks", "Dynamic Animated Subtitles", "Custom Soundscape Integration", "High retention cuts (Social-first)"),
            CyanBlue
          ),
          ServiceItem(
            "Long Form Content",
            "Cinematic Storytelling Sequence",
            "Designed for deep-dive YouTube Videos, Vlogs, Documentary style assemblies, Podcasts, and Corporate Ads.",
            listOf("Structural pacing orchestration", "Multi-camera sound sync", "Comprehensive stock overlays", "B-roll selection & integration"),
            ElectricPurple
          ),
          ServiceItem(
            "Professional Features",
            "Color Grading & Audio Masterclass",
            "Bringing raw flat vectors to life. Comprehensive HDR look development and premium auditory sound designs.",
            listOf("LUT profile calibration", "Frequency vocal leveling", "Custom intro/outro graphics", "3D virtual environment overlays"),
            VividPink
          )
        )

        Column(
          modifier = Modifier.padding(horizontal = 16.dp),
          verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          servicesList.forEach { service ->
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .background(
                  brush = Brush.radialGradient(
                    colors = listOf(service.accentColor.copy(alpha = 0.05f), Color.Transparent),
                    radius = 300f
                  ),
                  shape = RoundedCornerShape(24.dp)
                )
                .background(CosmicCard.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                .border(
                  BorderStroke(1.2.dp, service.accentColor.copy(alpha = 0.15f)),
                  RoundedCornerShape(24.dp)
                )
                .padding(20.dp)
            ) {
              Column {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Box(
                    modifier = Modifier
                      .size(42.dp)
                      .background(service.accentColor.copy(alpha = 0.15f), CircleShape)
                      .border(1.dp, service.accentColor, CircleShape),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = if (service.category.contains("Short")) Icons.Default.PlayArrow else if (service.category.contains("Long")) Icons.Default.List else Icons.Default.Settings,
                      contentDescription = service.category,
                      tint = service.accentColor,
                      modifier = Modifier.size(20.dp)
                    )
                  }
                  Spacer(modifier = Modifier.width(12.dp))
                  Column {
                    Text(
                      text = service.category.uppercase(),
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Black,
                      letterSpacing = 1.sp,
                      color = service.accentColor
                    )
                    Text(
                      text = service.title,
                      fontSize = 18.sp,
                      fontWeight = FontWeight.Bold,
                      color = GlowSilver
                    )
                  }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                  text = service.description,
                  fontSize = 13.sp,
                  color = TextMuted,
                  lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                  text = "INCLUDED DELIVERABLES:",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Black,
                  color = GlowSilver.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                service.subItems.forEach { subItem ->
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                  ) {
                    Box(
                      modifier = Modifier
                        .size(6.dp)
                        .background(service.accentColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                      text = subItem,
                      fontSize = 12.sp,
                      color = GlowSilver
                    )
                  }
                }
              }
            }
          }
        }


        // --- PORTFOLIO GALLERY SECTION WITH HOVER PREVIEW ---
        SectionHeading(title = "Cinematic Gallery", subtitle = "Recent high-converting video projects")

        // Interactive Categories selector
        val categories = listOf("All", "Instagram Reels", "YouTube Videos", "Gaming Edits", "Cinematic Edits", "Travel Videos", "Business Ads", "Motion Graphics")
        LazyRow(
          contentPadding = PaddingValues(horizontal = 16.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.padding(bottom = 16.dp)
        ) {
          items(categories) { filter ->
            val isSelected = filter == activePortfolioFilter
            Box(
              modifier = Modifier
                .background(
                  if (isSelected) ElectricPurple else CosmicCard,
                  RoundedCornerShape(100.dp)
                )
                .border(
                  BorderStroke(
                    1.dp,
                    if (isSelected) CyanBlue else Color.White.copy(alpha = 0.08f)
                  ),
                  RoundedCornerShape(100.dp)
                )
                .clickable { activePortfolioFilter = filter }
                .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
              Text(
                text = filter,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else GlowSilver.copy(alpha = 0.8f)
              )
            }
          }
        }

        // Mock Portfolio data items
        val allVideosList = listOf(
          PortfolioVideo("Ultimate Neon Cyberpunk Travel Edit", "Travel Videos", "0:45", "125k views", "May 2026", "Cinematic drone shots of Shibuya, Japan processed on DaVinci Resolve utilizing premium neon LUT profiles, smooth spatial zooms, and complex custom soundscapes.", "mock_url_1", isVertical = true),
          PortfolioVideo("Retainer YouTube Finance Script Integration", "YouTube Videos", "8:12", "720k views", "April 2026", "Fast-paced financial advice edit using highly animated text graphics, icons slides, background beats, and premium green screen overlays designed for maximum retention.", "mock_url_2"),
          PortfolioVideo("Retro Neon Gaming Montage Showcase", "Gaming Edits", "2:30", "1.4M views", "June 2026", "High-FPS gaming assembly. Keyframes synchronized with electronic synthesizers beats. Dynamic speed ramps and glowing hand-drawn After Effects overlays.", "mock_url_3"),
          PortfolioVideo("Modern Luminary Smartwatch Commercial", "Business Ads", "0:30", "2.1M views", "May 2026", "A professional advertising edit with premium color matching, 3D clock face track render, high contrast product exposure focus, and ambient acoustics.", "mock_url_4", isVertical = true),
          PortfolioVideo("Glow Cinematic Travel Vibe Reel", "Instagram Reels", "0:15", "980k views", "June 2026", "Super short aesthetic loop emphasizing organic film grains, light leaks, seamless audio transition, and retro sound designs.", "mock_url_5", isVertical = true),
          PortfolioVideo("Vector Kinetic Subtitle Demo", "Motion Graphics", "1:15", "80k views", "March 2026", "Showcase of fluid kinetic text layouts following conversational tones. Highly customized presets built completely within Adobe After Effects.", "mock_url_6")
        )

        val filteredVideos = if (activePortfolioFilter == "All") {
          allVideosList
        } else {
          allVideosList.filter { it.category == activePortfolioFilter || (activePortfolioFilter == "Instagram Reels" && it.isVertical) }
        }

        if (filteredVideos.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(140.dp)
              .padding(innerPadding),
            contentAlignment = Alignment.Center
          ) {
            Text("No projects mock-ups cataloged for category yet.", fontSize = 12.sp, color = TextMuted)
          }
        } else {
          val configuration = LocalConfiguration.current
          val screenWidth = configuration.screenWidthDp.dp
          val numColumns = if (screenWidth < 600.dp) 1 else if (screenWidth < 900.dp) 2 else 3

          val videoRows = filteredVideos.chunked(numColumns)

          Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
          ) {
            videoRows.forEach { rowItems ->
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top
              ) {
                rowItems.forEach { video ->
                  Box(
                    modifier = Modifier.weight(1f)
                  ) {
                    VideoGalleryItem(
                      video = video,
                      canvasTicks = canvasTicks,
                      onPlayClick = { selectedVideoForModal = it }
                    )
                  }
                }
                val emptySlots = numColumns - rowItems.size
                if (emptySlots > 0) {
                  repeat(emptySlots) {
                    Spacer(modifier = Modifier.weight(1f))
                  }
                }
              }
            }
          }
        }


        // --- DYNAMIC PRICING SECTION ---
        SectionHeading(title = "Transparent Packages", subtitle = "Affordable, industry-standard pricing tailored to your scale")

        Column(
          modifier = Modifier.padding(horizontal = 16.dp),
          verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          // Card 1: Reel Paket
          PricingCard(
            title = "Reel Editing Package",
            price = "₹800",
            pricePeriod = "Per Reel",
            features = listOf("Professional Editing", "Color Grading", "Sound Effects", "Smooth Transitions", "HD Export", "Fast Delivery"),
            accentColor = CyanBlue,
            buttonText = "Order Reel package",
            onSelect = {
              contactProjectType = "Reel / Short Editing"
              contactBudget = "Under ₹2,000"
              scope.launch { scrollState.animateScrollTo(8000) }
            }
          )

          // Card 2: Long format Paket
          PricingCard(
            title = "Long Video Package",
            price = "₹2,500",
            pricePeriod = "Starting Price",
            features = listOf("Professional Editing", "Motion Graphics", "Color Grading", "Audio Cleanup", "Subtitles", "High Quality Export"),
            accentColor = ElectricPurple,
            buttonText = "Order Long Video package",
            onSelect = {
              contactProjectType = "YouTube Video"
              contactBudget = "₹2,000 - ₹5,000"
              scope.launch { scrollState.animateScrollTo(8000) }
            }
          )

          // Card 3: Premium custom
          PricingCard(
            title = "Premium Package",
            price = "₹5,000+",
            pricePeriod = "Commercial scale",
            features = listOf("Advanced Editing", "Cinematic Effects", "Motion Graphics", "Custom Animations", "AI Enhancement", "Priority Delivery"),
            accentColor = VividPink,
            buttonText = "Request Custom Estimate",
            onSelect = {
              contactProjectType = "Cinematic travel edit"
              contactBudget = "₹5,000 - ₹10,000"
              scope.launch { scrollState.animateScrollTo(8000) }
            }
          )
        }


        // --- INTERACTIVE COST ESTIMATOR (Wow-Factor Widget) ---
        SectionHeading(title = "Instant Project Estimator", subtitle = "Calculate custom budget in real-time")

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
        ) {
          Column(
            modifier = Modifier
              .background(CosmicCard.copy(alpha = 0.9f), RoundedCornerShape(24.dp))
              .border(
                BorderStroke(1.2.dp, Brush.horizontalGradient(listOf(ElectricBlue, CyanBlue))),
                RoundedCornerShape(24.dp)
              )
              .padding(20.dp)
          ) {
            Text(
              text = "Dynamic Cost Calculator",
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = GlowSilver,
              modifier = Modifier.padding(bottom = 12.dp)
            )

            // Selector for video format
            Text("Select Video Format:", fontSize = 11.sp, color = TextMuted)
            Row(
              modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              val typesList = listOf("Reel / Short", "YouTube Video", "Commercial")
              typesList.forEach { vType ->
                val activeSel = vType == calcVideoType
                Box(
                  modifier = Modifier
                    .weight(1f)
                    .background(if (activeSel) ElectricBlue else CosmicSlate, RoundedCornerShape(8.dp))
                    .border(0.5.dp, if (activeSel) CyanBlue else Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    .clickable { calcVideoType = vType }
                    .padding(vertical = 8.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = vType,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (activeSel) Color.White else GlowSilver.copy(alpha = 0.7f)
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Duration Slider
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Target Video Length:",
                fontSize = 11.sp,
                color = TextMuted
              )
              Text(
                text = "${calcDurationMins.toInt()} min" + if (calcDurationMins.toInt() > 1) "s" else "",
                fontSize = 12.sp,
                color = CyanBlue,
                fontWeight = FontWeight.Bold
              )
            }

            Slider(
              value = calcDurationMins,
              onValueChange = { calcDurationMins = it },
              valueRange = 1f..30f,
              steps = 29,
              colors = SliderDefaults.colors(
                thumbColor = CyanBlue,
                activeTrackColor = ElectricBlue,
                inactiveTrackColor = CosmicSlate
              ),
              modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text("Select Addons & AI features:", fontSize = 11.sp, color = TextMuted)

            // Switch selections layout
            Column(
              verticalArrangement = Arrangement.spacedBy(4.dp),
              modifier = Modifier.padding(vertical = 4.dp)
            ) {
              InteractiveToggleRow("Epic Color Grading LUTs (+₹300)", calcAddGrading, { calcAddGrading = it })
              InteractiveToggleRow("AI Captions & Subtitle Style (+₹200)", calcAddSubtitles, { calcAddSubtitles = it })
              InteractiveToggleRow("Foley & Sound Effects Suite (+₹250)", calcAddSFX, { calcAddSFX = it })
              InteractiveToggleRow("3D Motion VFX Overlays (+₹800)", calcAddVFX, { calcAddVFX = it })
            }

            // Calculation Logic
            val finalCalculatedEst = remember(calcVideoType, calcDurationMins, calcAddGrading, calcAddSubtitles, calcAddSFX, calcAddVFX) {
              val basePrice = when (calcVideoType) {
                "Reel / Short" -> 800
                "YouTube Video" -> 2500
                else -> 4500
              }
              // Add a bit of multiplier for longer duration
              val durationPremium = if (calcVideoType != "Reel / Short") {
                (calcDurationMins.toInt() - 3).coerceAtLeast(0) * 150
              } else {
                (calcDurationMins.toInt() - 1).coerceAtLeast(0) * 200
              }

              var addonsSum = 0
              if (calcAddGrading) addonsSum += 300
              if (calcAddSubtitles) addonsSum += 200
              if (calcAddSFX) addonsSum += 250
              if (calcAddVFX) addonsSum += 800

              basePrice + durationPremium + addonsSum
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Calculated Estimate Box readout
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .padding(16.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text("ESTIMATED PRICE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                Text("₹$finalCalculatedEst", fontSize = 24.sp, fontWeight = FontWeight.Black, color = CyanBlue)
              }
              Button(
                onClick = {
                  contactProjectType = calcVideoType + " Editing"
                  contactBudget = "₹$finalCalculatedEst Estimate"
                  contactMessage = "Hi Aryan, I used your dynamic calculator. I'd like a custom editing quote for a ${calcDurationMins.toInt()} min $calcVideoType project with the selected overlays. Let's build!"
                  scope.launch { scrollState.animateScrollTo(8000) }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
                shape = RoundedCornerShape(10.dp)
              ) {
                Text("Order This", fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }


        // --- UNIQUE BENEFITS SECTION (Why choose Aryan) ---
        SectionHeading(title = "Why Choose Aryan?", subtitle = "Delivering unparalleled growth through high-end editing mechanics")

        val benefits = listOf(
          "Professional Quality Videos" to "Polished to Hollywood standards, rendering dynamic HDR grading and sharp clean outputs.",
          "Fast Turnaround Time" to "Your delivery occurs within strict client timelines without compromising on story structure or effects.",
          "Creative Editing Style" to "Unique and bespoke transitions, dynamic overlays, custom graphics, and energetic layouts.",
          "AI-Powered Workflow" to "Using neural networks matching voice synthesizers, intelligent upscaling, script flow optimizations.",
          "Audience Retention Techniques" to "Systematically designed hooks within the first 3 seconds to optimize continuous-swipe analytics.",
          "Modern Trending Effects" to "Fleshing trending captions, neon cyber lines, static retro film look developments."
        )

        Column(
          modifier = Modifier.padding(horizontal = 16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          benefits.forEachIndexed { idx, benefit ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .background(CosmicCard.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.04f)), RoundedCornerShape(16.dp))
                .padding(14.dp),
              verticalAlignment = Alignment.Top
            ) {
              Box(
                modifier = Modifier
                  .size(32.dp)
                  .background(ElectricBlue.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "${idx + 1}",
                  color = CyanBlue,
                  fontWeight = FontWeight.Black,
                  fontSize = 12.sp
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = benefit.first,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold,
                  color = GlowSilver
                )
                Text(
                  text = benefit.second,
                  fontSize = 11.sp,
                  lineHeight = 15.sp,
                  color = TextMuted,
                  modifier = Modifier.padding(top = 4.dp)
                )
              }
            }
          }
        }


        // --- CLIENT REVIEWS & INTERACTIVE RATINGS ---
        SectionHeading(title = "Client Endorsements", subtitle = "Outstanding 5.0 Rating ⭐⭐⭐⭐⭐")

        Column(
          modifier = Modifier.padding(horizontal = 16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          reviewsList.forEach { review ->
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .background(CosmicCard.copy(alpha = 0.7f), RoundedCornerShape(20.dp))
                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)), RoundedCornerShape(20.dp))
                .padding(16.dp)
            ) {
              Column {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.Top
                ) {
                  Column {
                    Text(
                      text = review.reviewerName,
                      fontSize = 13.sp,
                      fontWeight = FontWeight.Bold,
                      color = GlowSilver
                    )
                    Text(
                      text = review.role,
                      fontSize = 10.sp,
                      color = CyanBlue,
                      fontWeight = FontWeight.SemiBold
                    )
                  }
                  Text(
                    text = review.date,
                    fontSize = 9.sp,
                    color = TextMuted
                  )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Star Rating row
                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                  repeat(5) { starIndex ->
                    Icon(
                      imageVector = Icons.Default.Star,
                      contentDescription = "Rating Star",
                      tint = if (starIndex < review.rating) StarGold else Color.Gray,
                      modifier = Modifier.size(13.dp)
                    )
                  }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                  text = "\"${review.reviewText}\"",
                  fontSize = 12.sp,
                  lineHeight = 16.sp,
                  color = GlowSilver.copy(alpha = 0.9f),
                  fontStyle = FontStyle.Normal
                )
              }
            }
          }

          // Add review button trigger
          Button(
            onClick = { showAddReviewDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = CosmicSlate),
            border = BorderStroke(1.dp, ElectricPurple.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(Icons.Default.Add, contentDescription = "Add review icon", tint = CyanBlue)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Write a Review for Aryan", color = GlowSilver, fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
        }


        // --- SOCIAL CHANNELS SECTION ---
        SectionHeading(title = "Connect Socially", subtitle = "Connect with Aryan on social media platforms")

        val socialChannels = listOf(
          SocialIconData("Instagram", Icons.Default.Send, "https://instagram.com", VividPink),
          SocialIconData("YouTube", Icons.Default.PlayArrow, "https://youtube.com", VividPink),
          SocialIconData("Facebook", Icons.Default.Home, "https://facebook.com", ElectricBlue),
          SocialIconData("Twitter/X", Icons.Default.Share, "https://twitter.com", GlowSilver),
          SocialIconData("LinkedIn", Icons.Default.AccountBox, "https://linkedin.com", ElectricBlue),
          SocialIconData("WhatsApp", Icons.Default.Phone, "https://whatsapp.com", Color(0xFF25D366)),
          SocialIconData("Telegram", Icons.Default.Send, "https://telegram.org", ElectricBlue),
          SocialIconData("Discord", Icons.Default.Person, "https://discord.gg", ElectricBlue)
        )

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(CosmicCard.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .border(1.dp, Color.White.copy(alpha = 0.04f), RoundedCornerShape(20.dp))
            .padding(16.dp)
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = "Official Creative Handles",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = GlowSilver,
              modifier = Modifier.padding(bottom = 12.dp)
            )

            // Grid style layout for social
            socialChannels.chunked(4).forEach { chunk ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
              ) {
                chunk.forEach { ch ->
                  Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                      .clickable {
                        try {
                          uriHandler.openUri(ch.url)
                        } catch (ex: Exception) {
                          Toast
                            .makeText(context, "Redirecting to ${ch.name}", Toast.LENGTH_SHORT)
                            .show()
                        }
                      }
                      .padding(8.dp)
                  ) {
                    Box(
                      modifier = Modifier
                        .size(38.dp)
                        .background(ch.color.copy(alpha = 0.15f), CircleShape)
                        .border(1.dp, ch.color.copy(alpha = 0.4f), CircleShape),
                      contentAlignment = Alignment.Center
                    ) {
                      Icon(
                        imageVector = ch.icon,
                        contentDescription = ch.name,
                        tint = ch.color,
                        modifier = Modifier.size(16.dp)
                      )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = ch.name, fontSize = 9.sp, color = TextMuted)
                  }
                }
              }
            }
          }
        }


        // --- CONTACT SECTION FORM ---
        SectionHeading(title = "Secure Booking Console", subtitle = "Send custom messages or request client retainer spots")

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
          Column(
            modifier = Modifier
              .background(CosmicCard, RoundedCornerShape(24.dp))
              .border(
                BorderStroke(
                  1.2.dp,
                  Brush.horizontalGradient(listOf(ElectricPurple, VividPink))
                ),
                RoundedCornerShape(24.dp)
              )
              .padding(20.dp)
          ) {
            Text(
              text = "Client Inquiry Form",
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = GlowSilver,
              modifier = Modifier.padding(bottom = 12.dp)
            )

            // Text inputs
            Text("Full Name", fontSize = 11.sp, color = TextMuted, modifier = Modifier.padding(bottom = 2.dp))
            OutlinedTextField(
              value = contactName,
              onValueChange = { contactName = it },
              placeholder = { Text("Enter your name", color = GlowSilver.copy(alpha = 0.3f), fontSize = 12.sp) },
              modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
              shape = RoundedCornerShape(10.dp),
              textStyle = TextStyle(color = GlowSilver, fontSize = 13.sp),
              colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                focusedBorderColor = ElectricPurple,
                unfocusedContainerColor = CosmicSlate,
                focusedContainerColor = CosmicSlate
              ),
              maxLines = 1
            )

            Text("Email Address", fontSize = 11.sp, color = TextMuted, modifier = Modifier.padding(bottom = 2.dp))
            OutlinedTextField(
              value = contactEmail,
              onValueChange = { contactEmail = it },
              placeholder = { Text("example@domain.com", color = GlowSilver.copy(alpha = 0.3f), fontSize = 12.sp) },
              modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
              shape = RoundedCornerShape(10.dp),
              textStyle = TextStyle(color = GlowSilver, fontSize = 13.sp),
              colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                focusedBorderColor = ElectricPurple,
                unfocusedContainerColor = CosmicSlate,
                focusedContainerColor = CosmicSlate
              ),
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
              maxLines = 1
            )

            Text("Phone Number", fontSize = 11.sp, color = TextMuted, modifier = Modifier.padding(bottom = 2.dp))
            OutlinedTextField(
              value = contactPhone,
              onValueChange = { contactPhone = it },
              placeholder = { Text("E.g. +91 9876543210", color = GlowSilver.copy(alpha = 0.3f), fontSize = 12.sp) },
              modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
              shape = RoundedCornerShape(10.dp),
              textStyle = TextStyle(color = GlowSilver, fontSize = 13.sp),
              colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                focusedBorderColor = ElectricPurple,
                unfocusedContainerColor = CosmicSlate,
                focusedContainerColor = CosmicSlate
              ),
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
              maxLines = 1
            )

            // Horizontal project type select
            Text("Project Type", fontSize = 11.sp, color = TextMuted, modifier = Modifier.padding(bottom = 4.dp))
            LazyRow(
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              modifier = Modifier.padding(bottom = 12.dp)
            ) {
              items(projectTypes) { pType ->
                val s = pType == contactProjectType
                Box(
                  modifier = Modifier
                    .background(if (s) ElectricPurple else CosmicSlate, RoundedCornerShape(8.dp))
                    .border(0.5.dp, if (s) CyanBlue else Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                    .clickable { contactProjectType = pType }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                  Text(pType, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = GlowSilver)
                }
              }
            }

            // Budget scale select
            Text("Target Budget Scale", fontSize = 11.sp, color = TextMuted, modifier = Modifier.padding(bottom = 4.dp))
            LazyRow(
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              modifier = Modifier.padding(bottom = 12.dp)
            ) {
              items(budgetsOptions) { bg ->
                val s = bg == contactBudget
                Box(
                  modifier = Modifier
                    .background(if (s) ElectricBlue else CosmicSlate, RoundedCornerShape(8.dp))
                    .border(0.5.dp, if (s) CyanBlue else Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                    .clickable { contactBudget = bg }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                  Text(bg, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = GlowSilver)
                }
              }
            }

            Text("Your Message / Request details", fontSize = 11.sp, color = TextMuted, modifier = Modifier.padding(bottom = 2.dp))
            OutlinedTextField(
              value = contactMessage,
              onValueChange = { contactMessage = it },
              placeholder = { Text("What stories are we telling today?", color = GlowSilver.copy(alpha = 0.3f), fontSize = 12.sp) },
              modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(bottom = 16.dp),
              shape = RoundedCornerShape(10.dp),
              textStyle = TextStyle(color = GlowSilver, fontSize = 13.sp),
              colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                focusedBorderColor = ElectricPurple,
                unfocusedContainerColor = CosmicSlate,
                focusedContainerColor = CosmicSlate
              ),
              maxLines = 4
            )

            // Submit Button
            Button(
              onClick = {
                // Validation Checklist
                if (contactName.isBlank() || contactEmail.isBlank() || !contactEmail.contains("@")) {
                  showFeedbackError = true
                } else {
                  showFeedbackDialog = true
                }
              },
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
            ) {
              Icon(Icons.Default.Send, contentDescription = "Send Message Client", tint = Color.White)
              Spacer(modifier = Modifier.width(8.dp))
              Text("Send Message", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
          }
        }


        // --- FOOTER BRAND PANEL ---
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(CosmicSlate)
            .padding(horizontal = 24.dp, vertical = 32.dp)
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = "Aryan Editor",
              fontSize = 24.sp,
              fontWeight = FontWeight.Black,
              style = TextStyle(
                brush = purplePinkGrad
              )
            )

            Text(
              text = "uxaryanedit",
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              color = CyanBlue,
              modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            Text(
              text = "Creating Videos That Capture Attention And Drive Results.",
              fontSize = 12.sp,
              color = GlowSilver.copy(alpha = 0.7f),
              textAlign = TextAlign.Center,
              modifier = Modifier.padding(horizontal = 8.dp).padding(bottom = 18.dp)
            )

            // Contact directly
            Text("Direct Line: contact@aryaneditor.site", fontSize = 11.sp, color = TextMuted)
            Text("Whatsapp Support: +91 91028302XX", fontSize = 11.sp, color = TextMuted, modifier = Modifier.padding(top = 2.dp, bottom = 18.dp))

            // Social media row in footer
            Row(
              horizontalArrangement = Arrangement.spacedBy(16.dp),
              modifier = Modifier.padding(bottom = 24.dp)
            ) {
              val chunkSocials = socialChannels.take(5)
              chunkSocials.forEach { ch ->
                Box(
                  modifier = Modifier
                    .size(34.dp)
                    .background(Color.White.copy(alpha = 0.04f), CircleShape)
                    .clickable {
                      try {
                        uriHandler.openUri(ch.url)
                      } catch (ex: Exception) {}
                    },
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = ch.icon,
                    contentDescription = ch.name,
                    tint = GlowSilver.copy(alpha = 0.8f),
                    modifier = Modifier.size(14.dp)
                  )
                }
              }
            }

            Text(
              text = "Aryan Editor ©diwakarramu510",
              fontSize = 10.sp,
              color = TextMuted.copy(alpha = 0.5f),
              fontWeight = FontWeight.Light
            )
          }
        }

      } // End of Column scrollable

      // Dialog: Client Feedback validation issue
      if (showFeedbackError) {
        Dialog(
          onDismissRequest = { showFeedbackError = false }
        ) {
          Box(
            modifier = Modifier
              .background(CosmicCard, RoundedCornerShape(20.dp))
              .border(1.dp, VividPink, RoundedCornerShape(20.dp))
              .padding(20.dp)
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Box(
                modifier = Modifier
                  .size(46.dp)
                  .background(VividPink.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Default.Warning, "Error info", tint = VividPink, modifier = Modifier.size(24.dp))
              }
              Spacer(modifier = Modifier.height(14.dp))
              Text("Validation Checklist Failed", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = GlowSilver)
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                "Please fill in both Name and a valid Email address so Aryan can reach back with your custom proposal.",
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                color = TextMuted,
                lineHeight = 16.sp
              )
              Spacer(modifier = Modifier.height(16.dp))
              Button(
                onClick = { showFeedbackError = false },
                colors = ButtonDefaults.buttonColors(containerColor = VividPink),
                shape = RoundedCornerShape(10.dp)
              ) {
                Text("Retry Form")
              }
            }
          }
        }
      }

      // Dialog: Client Feedback success message transmitted
      if (showFeedbackDialog) {
        Dialog(
          onDismissRequest = {
            showFeedbackDialog = false
            // Reset fields
            contactName = ""
            contactEmail = ""
            contactPhone = ""
            contactMessage = ""
          }
        ) {
          Box(
            modifier = Modifier
              .background(CosmicCard, RoundedCornerShape(24.dp))
              .border(1.dp, CyanBlue, RoundedCornerShape(24.dp))
              .padding(24.dp)
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Box(
                modifier = Modifier
                  .size(54.dp)
                  .background(CyanBlue.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Default.Check, "Success logo", tint = CyanBlue, modifier = Modifier.size(32.dp))
              }
              Spacer(modifier = Modifier.height(16.dp))
              Text(
                text = "Proposal Transmitted!",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = GlowSilver
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "Thank you $contactName. Your detailed project briefing is encrypted and dispatched directly to Aryan. He will connect with you shortly with draft boards ideas.",
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                color = TextMuted,
                lineHeight = 18.sp
              )
              Spacer(modifier = Modifier.height(18.dp))
              Button(
                onClick = {
                  showFeedbackDialog = false
                  // Reset fields
                  contactName = ""
                  contactEmail = ""
                  contactPhone = ""
                  contactMessage = ""
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                Text("Great, Close Desk")
              }
            }
          }
        }
      }

      // Dialog: Real-time custom Review submission dialog
      if (showAddReviewDialog) {
        var newReviewName by remember { mutableStateOf("") }
        var newReviewRole by remember { mutableStateOf("") }
        var newReviewText by remember { mutableStateOf("") }
        var newReviewRating by remember { mutableStateOf(5) }
        var reviewSubError by remember { mutableStateOf("") }

        Dialog(
          onDismissRequest = { showAddReviewDialog = false }
        ) {
          Box(
            modifier = Modifier
              .background(CosmicCard, RoundedCornerShape(24.dp))
              .border(1.dp, DynamicBorderBrush(), RoundedCornerShape(24.dp))
              .padding(20.dp)
          ) {
            Column {
              Text("Post Client Review", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GlowSilver)
              Divider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 10.dp))

              Text("Reviewer Name", fontSize = 11.sp, color = TextMuted)
              OutlinedTextField(
                value = newReviewName,
                onValueChange = { newReviewName = it },
                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                shape = RoundedCornerShape(8.dp),
                textStyle = TextStyle(color = GlowSilver, fontSize = 12.sp),
                colors = OutlinedTextFieldDefaults.colors(
                  unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                  focusedBorderColor = ElectricPurple
                ),
                maxLines = 1
              )

              Text("Role (e.g. YouTube Vlogger / Brand Mgr)", fontSize = 11.sp, color = TextMuted)
              OutlinedTextField(
                value = newReviewRole,
                onValueChange = { newReviewRole = it },
                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                shape = RoundedCornerShape(8.dp),
                textStyle = TextStyle(color = GlowSilver, fontSize = 12.sp),
                colors = OutlinedTextFieldDefaults.colors(
                  unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                  focusedBorderColor = ElectricPurple
                ),
                maxLines = 1
              )

              // Stars selector click
              Text("Assign Stars Rating:", fontSize = 11.sp, color = TextMuted, modifier = Modifier.padding(bottom = 4.dp))
              Row(
                modifier = Modifier.padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                repeat(5) { starIdx ->
                  val activeStar = starIdx < newReviewRating
                  Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "RatingStarSelector",
                    tint = if (activeStar) StarGold else Color.Gray,
                    modifier = Modifier
                      .size(26.dp)
                      .clickable { newReviewRating = starIdx + 1 }
                  )
                }
              }

              Text("Review Text Details", fontSize = 11.sp, color = TextMuted)
              OutlinedTextField(
                value = newReviewText,
                onValueChange = { newReviewText = it },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(80.dp)
                  .padding(bottom = 10.dp),
                shape = RoundedCornerShape(8.dp),
                textStyle = TextStyle(color = GlowSilver, fontSize = 12.sp),
                colors = OutlinedTextFieldDefaults.colors(
                  unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                  focusedBorderColor = ElectricPurple
                ),
                maxLines = 3
              )

              if (reviewSubError.isNotEmpty()) {
                Text(reviewSubError, color = VividPink, fontSize = 10.sp, modifier = Modifier.padding(bottom = 8.dp))
              }

              Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                  onClick = { showAddReviewDialog = false },
                  modifier = Modifier.weight(1f).padding(end = 4.dp),
                  shape = RoundedCornerShape(8.dp)
                ) {
                  Text("Cancel", color = GlowSilver)
                }
                Button(
                  onClick = {
                    if (newReviewName.isBlank() || newReviewText.isBlank()) {
                      reviewSubError = "Please complete Name and Review Content."
                    } else {
                      reviewsList.add(
                        0, // Post to the front dynamically
                        ClientReview(
                          newReviewName,
                          if (newReviewRole.isBlank()) "Creator" else newReviewRole,
                          newReviewRating,
                          newReviewText,
                          "June 2026"
                        )
                      )
                      showAddReviewDialog = false
                    }
                  },
                  modifier = Modifier.weight(1f).padding(start = 4.dp),
                  colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
                  shape = RoundedCornerShape(8.dp)
                ) {
                  Text("Submit Review")
                }
              }
            }
          }
        }
      }

      // Dialog: Immersive Portfolio Simulated Video Player Modal
      selectedVideoForModal?.let { video ->
        var simulatedPlayState by remember { mutableStateOf(true) }
        var simulatedTimestampSec by remember { mutableStateOf(12) }

        // Simulated time tracker
        LaunchedEffect(simulatedPlayState) {
          if (simulatedPlayState) {
            while (true) {
              withFrameMillis {
                // Approximate timing tick
              }
              kotlinx.coroutines.delay(1000)
              simulatedTimestampSec = (simulatedTimestampSec + 1) % 45
            }
          }
        }

        Dialog(
          onDismissRequest = { selectedVideoForModal = null },
          properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(Color.Black.copy(alpha = 0.95f))
              .padding(innerPadding)
              .padding(16.dp)
          ) {
            Column(modifier = Modifier.fillMaxSize()) {
              // Modal Header with Close triggers
              Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(text = "INTERACTIVE LAYER WORKFLOW", fontSize = 10.sp, color = CyanBlue, fontWeight = FontWeight.Bold)
                  Text(text = video.title, fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White)
                }
                IconButton(onClick = { selectedVideoForModal = null }) {
                  Icon(Icons.Default.Close, contentDescription = "Close player", tint = Color.White)
                }
              }

              // Active Simulated Canvas "Video Player" Screen area
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .weight(1f) // Fill remaining space dynamically
                  .background(CosmicSlate, RoundedCornerShape(20.dp))
                  .border(BorderStroke(1.dp, AxisBrush()), RoundedCornerShape(20.dp))
                  .clip(RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
              ) {
                // Interactive player visual effects
                Column(
                  modifier = Modifier.fillMaxSize(),
                  verticalArrangement = Arrangement.Center,
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  // Central Pulsing state depending on play toggle
                  Box(
                    modifier = Modifier
                      .size(64.dp)
                      .background(Color.Black.copy(alpha = 0.7f), CircleShape)
                      .border(1.dp, CyanBlue, CircleShape)
                      .clickable { simulatedPlayState = !simulatedPlayState },
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = if (simulatedPlayState) Icons.Default.PlayArrow else Icons.Default.PlayArrow, // Draw custom shapes
                      contentDescription = "Simulated play indicator",
                      tint = if (simulatedPlayState) Color(0xFF00FF66) else Color.White,
                      modifier = Modifier
                        .size(32.dp)
                        .scale(if (simulatedPlayState) 1f else 1.2f)
                    )
                  }

                  Spacer(modifier = Modifier.height(20.dp))

                  // Mock color grading representation before/after visual
                  Row(
                    modifier = Modifier
                      .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                      .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text("Original Log", fontSize = 10.sp, color = video.colorGradingBefore)
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(modifier = Modifier.size(24.dp, 2.dp).background(Color.White.copy(alpha = 0.3f)))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Aryan Color Grading Vibe", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = video.colorGradingAfter)
                  }

                  // Waveforms display simulator
                  Box(
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(50.dp)
                      .padding(top = 16.dp).padding(horizontal = 20.dp)
                      .alpha(0.6f)
                      .drawBehind {
                        // Drawing retro sound channels
                        val channelHeight = size.height * 0.5f
                        val waveWidthStep = 10f
                        var startX = 0f
                        while (startX < size.width) {
                          val wavePower = (kotlin.math.sin(startX * 0.05f + canvasTicks) + 1f) * channelHeight
                          drawLine(
                            color = CyanBlue,
                            start = Offset(startX, size.height * 0.5f - wavePower * 0.4f),
                            end = Offset(startX, size.height * 0.5f + wavePower * 0.4f),
                            strokeWidth = 3f,
                            cap = StrokeCap.Round
                          )
                          startX += waveWidthStep
                        }
                      }
                  )
                }

                // Subtitle Overlay text loop simulation
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(12.dp)
                ) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = getSubtitleForSecond(simulatedTimestampSec),
                      fontSize = 11.sp,
                      color = GlowSilver,
                      fontWeight = FontWeight.Bold,
                      textAlign = TextAlign.Center,
                      modifier = Modifier.weight(1f)
                    )
                    Text(
                      text = "0:${simulatedTimestampSec.toString().padStart(2, '0')} / ${video.duration}",
                      fontSize = 10.sp,
                      fontFamily = FontFamily.Monospace,
                      color = CyanBlue
                    )
                  }
                }
              }

              // Editor Timeline Tracks Inspector (Simulated timeline tracks inside resolve/premiere)
              Spacer(modifier = Modifier.height(12.dp))
              Text("Timeline Track breakdown (Aryan's layer setup):", fontSize = 11.sp, color = TextMuted)
              Spacer(modifier = Modifier.height(6.dp))

              Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                  .background(CosmicCard, RoundedCornerShape(14.dp))
                  .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(14.dp))
                  .padding(12.dp)
              ) {
                video.timelineTracks.forEachIndexed { iIndex, trackName ->
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Box(
                      modifier = Modifier
                        .size(14.dp)
                        .background(
                          if (iIndex == 0) ElectricPurple else if (iIndex == 1) ElectricBlue else Color.Gray,
                          RoundedCornerShape(3.dp)
                        ),
                      contentAlignment = Alignment.Center
                    ) {
                      Text(
                        text = "V${iIndex + 1}",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                      )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(
                      modifier = Modifier
                        .weight(1f)
                        .background(CosmicSlate, RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                      Text(trackName, fontSize = 10.sp, color = GlowSilver)
                    }
                  }
                }
              }

              // Modal bottom control actions booking
              Spacer(modifier = Modifier.height(16.dp))
              Button(
                onClick = {
                  contactProjectType = video.category + " Project"
                  contactMessage = "Hi Aryan! I audited your ${video.title} layer structure in your app portfolio. I'd like to book similar timeline services."
                  selectedVideoForModal = null
                  scope.launch { scrollState.animateScrollTo(8000) }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
                shape = RoundedCornerShape(12.dp)
              ) {
                Text("Order similar Video style", fontSize = 13.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }

    } // End of viewport box
  }
}

// Subtitle simulation generator depending on seconds active
fun getSubtitleForSecond(sec: Int): String {
  return when (sec % 45) {
    in 0..4 -> "🔥 [Upbeat Beat Drops] Transforming ordinary footage into high-converting stories..."
    in 5..9 -> "⚡ Retention editing isn't about fast cuts — it's about story sync."
    in 10..14 -> "🎨 Matching raw flat colors to cinema-grade custom color LUTs."
    in 15..19 -> "🔊 Adding localized foley and atmospheric SFX soundscapes."
    in 20..24 -> "🤖 Blending ElevenLabs vocoders and custom AI content enhancements!"
    in 25..29 -> "📈 Disrupting swiping habits within the critical first three seconds."
    in 30..34 -> "✨ Resulting in elevated views, clicks, and brand conversions."
    else -> "📈 Let's launch your channel to the next level today!"
  }
}

// Subcomponent: Section heading
@Composable
fun SectionHeading(title: String, subtitle: String) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 24.dp)
  ) {
    Text(
      text = title,
      fontSize = 24.sp,
      fontWeight = FontWeight.Black,
      color = GlowSilver,
      style = TextStyle(letterSpacing = 0.5.sp)
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = subtitle,
      fontSize = 12.sp,
      color = TextMuted,
      lineHeight = 16.sp
    )
    Spacer(modifier = Modifier.height(8.dp))
    Box(
      modifier = Modifier
        .size(60.dp, 3.dp)
        .background(Brush.horizontalGradient(listOf(ElectricPurple, CyanBlue)))
    )
  }
}

// Subcomponent: Progress skill bar
@Composable
fun SkillProgressBar(name: String, percent: Float, progressColor: Color) {
  Column(modifier = Modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(text = name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GlowSilver)
      Text(text = "${(percent * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Black, color = progressColor)
    }
    Spacer(modifier = Modifier.height(6.dp))
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(8.dp)
        .background(CosmicSlate, RoundedCornerShape(100.dp))
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth(percent)
          .fillMaxHeight()
          .background(progressColor, RoundedCornerShape(100.dp))
      )
    }
  }
}

// Subcomponent: Pricing Card
@Composable
fun PricingCard(
  title: String,
  price: String,
  pricePeriod: String,
  features: List<String>,
  accentColor: Color,
  buttonText: String,
  onSelect: () -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .background(CosmicCard.copy(alpha = 0.8f), RoundedCornerShape(24.dp))
      .border(BorderStroke(1.2.dp, Color.White.copy(alpha = 0.06f)), RoundedCornerShape(24.dp))
      .padding(20.dp)
  ) {
    Column {
      Text(
        text = title.uppercase(),
        fontSize = 11.sp,
        fontWeight = FontWeight.Black,
        color = accentColor,
        letterSpacing = 1.sp
      )

      Row(
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.padding(vertical = 8.dp)
      ) {
        Text(
          text = price,
          fontSize = 28.sp,
          fontWeight = FontWeight.Black,
          color = GlowSilver
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "/ $pricePeriod",
          fontSize = 12.sp,
          color = TextMuted,
          modifier = Modifier.padding(bottom = 4.dp)
        )
      }

      Divider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 12.dp))

      features.forEach { ft ->
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(vertical = 4.dp)
            .fillMaxWidth()
        ) {
          Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Eligible feature check",
            tint = accentColor,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(text = ft, fontSize = 12.sp, color = GlowSilver)
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      Button(
        onClick = onSelect,
        colors = ButtonDefaults.buttonColors(containerColor = CosmicSlate),
        border = BorderStroke(1.dp, accentColor),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(46.dp)
      ) {
        Text(text = buttonText, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GlowSilver)
      }
    }
  }
}

// Subcomponent: Toggle Row for Cost calculation selection
@Composable
fun InteractiveToggleRow(
  label: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onCheckedChange(!checked) }
      .padding(vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(text = label, fontSize = 11.sp, color = GlowSilver.copy(alpha = 0.85f))
    Box(
      modifier = Modifier
        .size(34.dp, 20.dp)
        .background(if (checked) ElectricBlue else CosmicSlate, RoundedCornerShape(100.dp))
        .border(1.dp, if (checked) CyanBlue else Color.White.copy(alpha = 0.15f), RoundedCornerShape(100.dp))
        .padding(horizontal = 2.dp),
      contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart
    ) {
      Box(
        modifier = Modifier
          .size(14.dp)
          .background(Color.White, CircleShape)
      )
    }
  }
}

// Secondary Custom Helper Styles
data class SocialIconData(
  val name: String,
  val icon: ImageVector,
  val url: String,
  val color: Color
)

@Composable
fun DynamicBorderBrush(): Brush {
  return Brush.sweepGradient(
    listOf(ElectricPurple, CyanBlue, VividPink, ElectricPurple)
  )
}

@Composable
fun AxisBrush(): Brush {
  return Brush.linearGradient(
    listOf(Color.White.copy(alpha = 0.05f), ElectricPurple.copy(alpha = 0.2f), Color.White.copy(alpha = 0.05f))
  )
}

@Composable
fun VideoGalleryItem(
  video: PortfolioVideo,
  canvasTicks: Float,
  onPlayClick: (PortfolioVideo) -> Unit
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isHovered by interactionSource.collectIsHoveredAsState()
  val isPressed by interactionSource.collectIsPressedAsState()
  val isFocused by interactionSource.collectIsFocusedAsState()
  val isItemActive = isHovered || isPressed || isFocused

  val animatedScale by animateFloatAsState(
    targetValue = if (isItemActive) 1.04f else 1.0f,
    animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessLow),
    label = "ScaleAnimation"
  )

  val transition = rememberInfiniteTransition(label = "ScrubbingTransition")
  val rawPlayheadProgress by transition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 4000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "PlayheadPosition"
  )

  val playheadProgress = if (isItemActive) rawPlayheadProgress else 0.15f

  val displayedViewsAndDate = if (isItemActive) {
    "Auto-Scrubbing Preview"
  } else {
    "${video.views} • Delivered ${video.date}"
  }

  val displayDuration = if (isItemActive) {
    val totalSeconds = 45
    val currentSec = (playheadProgress * totalSeconds).toInt()
    "0:${currentSec.toString().padStart(2, '0')}"
  } else {
    video.duration
  }

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .scale(animatedScale)
      .background(CosmicCard, RoundedCornerShape(20.dp))
      .border(
        BorderStroke(
          if (isItemActive) 1.5.dp else 1.dp,
          if (isItemActive) {
            Brush.horizontalGradient(listOf(ElectricPurple, CyanBlue, VividPink))
          } else {
            Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.08f), Color.White.copy(alpha = 0.04f)))
          }
        ),
        RoundedCornerShape(20.dp)
      )
      .clickable(
        interactionSource = interactionSource,
        indication = null,
        onClick = { onPlayClick(video) }
      )
      .clip(RoundedCornerShape(20.dp))
  ) {
    Column {
      // Beautiful Visual Simulated Video Frame Box
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(145.dp)
          .background(
            Brush.linearGradient(
              colors = if (video.isVertical) {
                listOf(CosmicSlate, ElectricPurple.copy(alpha = if (isItemActive) 0.5f else 0.25f))
              } else {
                listOf(CosmicSlate, ElectricBlue.copy(alpha = if (isItemActive) 0.4f else 0.2f))
              }
            )
          )
          .drawBehind {
            // Draw scanning grids if active
            if (isItemActive) {
              val itemWidth = size.width
              val itemHeight = size.height

              // Draw a vertical scanning playhead laser line
              val laserX = playheadProgress * itemWidth
              drawLine(
                color = CyanBlue.copy(alpha = 0.6f),
                start = Offset(laserX, 0f),
                end = Offset(laserX, itemHeight),
                strokeWidth = 3f
              )

              // Draw subtle neon shading around playhead
              drawRect(
                color = CyanBlue.copy(alpha = 0.06f),
                topLeft = Offset(0f, 0f),
                size = androidx.compose.ui.geometry.Size(laserX, itemHeight)
              )
            }

            // Draw audio/video wave path animation
            val strokePath = Path().apply {
              val amplitude = if (isItemActive) size.height * 0.25f else size.height * 0.12f
              val centerY = size.height * 0.65f
              val wavelength = size.width * 0.5f
              val offsetPhase = if (isItemActive) canvasTicks * 5f else canvasTicks * 1.5f

              moveTo(0f, centerY)
              for (x in 0..size.width.toInt() step 5) {
                val y = centerY + amplitude * kotlin.math.sin((x / wavelength) * 2 * kotlin.math.PI.toFloat() - offsetPhase)
                lineTo(x.toFloat(), y)
              }
            }
            drawPath(
              path = strokePath,
              color = if (isItemActive) Color(0xFF00FF66).copy(alpha = 0.35f) else CyanBlue.copy(alpha = 0.15f),
              style = Stroke(width = if (isItemActive) 5f else 3f, cap = StrokeCap.Round)
            )
          },
        contentAlignment = Alignment.Center
      ) {
        // Video Metadata details layer
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
          ) {
            Box(
              modifier = Modifier
                .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(6.dp))
                .border(0.5.dp, if (isItemActive) ElectricPurple else Color.Transparent, RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(6.dp)
                    .background(if (video.isVertical) VividPink else ElectricBlue, CircleShape)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(video.category, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = GlowSilver)
              }
            }
            Box(
              modifier = Modifier
                .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text(
                text = displayDuration,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = if (isItemActive) Color(0xFF00FF66) else GlowSilver,
                fontFamily = FontFamily.Monospace
              )
            }
          }

          // Interactive HUD elements
          if (isItemActive) {
            Box(
              modifier = Modifier
                .align(Alignment.TopCenter)
                .background(VividPink.copy(alpha = 0.9f), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 1.5.dp)
            ) {
              Text(
                text = "ACTIVE TIMELINE PREVIEW",
                fontSize = 8.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 0.5.sp
              )
            }
          }
        }

        // Play Circular Button in center
        val playButtonScale by animateFloatAsState(
          targetValue = if (isItemActive) 1.15f else 1.0f,
          label = "PlayButtonScale"
        )
        Box(
          modifier = Modifier
            .size(52.dp)
            .scale(playButtonScale)
            .background(Color.Black.copy(alpha = 0.75f), CircleShape)
            .border(
              1.5.dp,
              if (isItemActive) {
                Brush.sweepGradient(listOf(ElectricPurple, CyanBlue, VividPink, ElectricPurple))
              } else {
                Brush.horizontalGradient(listOf(ElectricPurple, CyanBlue))
              },
              CircleShape
            ),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Interact play",
            tint = if (isItemActive) Color(0xFF00FF66) else CyanBlue,
            modifier = Modifier.size(26.dp)
          )
        }

        // Bottom info label in thumbnail
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .background(Color.Black.copy(alpha = 0.6f))
            .padding(vertical = 4.dp, horizontal = 12.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = displayedViewsAndDate,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = if (isItemActive) Color(0xFF00FF66) else CyanBlue
            )
            if (isItemActive) {
              Text(
                text = "SCRUBBING",
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                color = CyanBlue,
                letterSpacing = 0.5.sp
              )
            } else {
              Text(
                text = "PREVIEW",
                fontSize = 9.sp,
                color = TextMuted
              )
            }
          }
        }
      }

      // Description section below thumbnail
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(if (isItemActive) CosmicSlate.copy(alpha = 0.4f) else Color.Transparent)
          .padding(16.dp)
      ) {
        Text(
          text = video.title,
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = if (isItemActive) Color.White else GlowSilver,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = video.description,
          fontSize = 11.sp,
          lineHeight = 15.sp,
          color = if (isItemActive) GlowSilver.copy(alpha = 0.8f) else TextMuted,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          Icon(
            imageVector = if (isItemActive) Icons.Default.CheckCircle else Icons.Default.PlayArrow,
            contentDescription = "Trigger hint",
            tint = if (isItemActive) Color(0xFF00FF66) else ElectricPurple,
            modifier = Modifier.size(12.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = if (isItemActive) {
              "Release or tap to expand pipeline & track layers"
            } else {
              "Hover or hold to scrub preview timeline"
            },
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = if (isItemActive) Color(0xFF00FF66) else ElectricPurple,
            style = TextStyle(letterSpacing = 0.3.sp)
          )
        }
      }
    }
  }
}
