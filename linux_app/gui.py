from PyQt6.QtWidgets import (QMainWindow, QWidget, QVBoxLayout, QHBoxLayout,
                             QLabel)
from PyQt6.QtCore import Qt, pyqtSignal
from PyQt6.QtGui import QFont

FACES = {
    "neutral": "(  •_•)",
    "happy": "(◕‿‿◕)",
    "sad": "(  •︵•)",
    "bored": "( ⇀‿‿↼)",
    "sleeping": "( ✖‿‿✖)",
    "observing": "( ⊙‿‿⊙)",
    "cool": "(  ⌐■_■)",
    "motivated": "( ✜‿‿✜)",
    "demotivated": "( ✖_✖ )",
    "smart": "( ✜_✜ )",
    "lonely": "(  •__•)",
    "broken": "(  X_X )",
    "debug": "(  #_# )"
}

class PwnagotchiWindow(QMainWindow):
    """
    Main GUI Window for the Pwnagotchi Linux App.
    Displays the face and statistics.
    """
    # Signal to receive updates from non-GUI thread
    update_signal = pyqtSignal(dict)

    def __init__(self):
        super().__init__()
        self.setWindowTitle("Pwnagotchi Linux")
        self.setMinimumSize(400, 300)
        self.setStyleSheet("background-color: #000000; color: #FFFFFF;")

        central_widget = QWidget()
        self.setCentralWidget(central_widget)

        main_layout = QVBoxLayout(central_widget)

        # Font setup
        self.font_mono = QFont("Monospace", 14)
        self.font_mono.setStyleHint(QFont.StyleHint.Monospace)
        self.font_face = QFont("Monospace", 48)
        self.font_face.setBold(True)

        # Status Line 1
        self.status_layout = QHBoxLayout()
        self.label_ch = QLabel("CH: -")
        self.label_aps = QLabel("APS: 0")
        self.label_up = QLabel("UP: 00:00:00")

        for lbl in [self.label_ch, self.label_aps, self.label_up]:
            lbl.setFont(self.font_mono)
            self.status_layout.addWidget(lbl)
            self.status_layout.addStretch()

        # Remove last stretch
        if self.status_layout.count() > 0:
            self.status_layout.takeAt(self.status_layout.count()-1)

        main_layout.addLayout(self.status_layout)

        # Face
        self.label_face = QLabel(FACES["neutral"])
        self.label_face.setFont(self.font_face)
        self.label_face.setAlignment(Qt.AlignmentFlag.AlignCenter)
        main_layout.addWidget(self.label_face, 1) # Expandable

        # Status Line 2
        self.status_layout2 = QHBoxLayout()
        self.label_pwnd = QLabel("PWND: 0")
        self.label_mode = QLabel("MANU")

        for lbl in [self.label_pwnd, self.label_mode]:
            lbl.setFont(self.font_mono)
            self.status_layout2.addWidget(lbl)
            self.status_layout2.addStretch()

        if self.status_layout2.count() > 0:
            self.status_layout2.takeAt(self.status_layout2.count()-1)

        main_layout.addLayout(self.status_layout2)

        # Connect signal
        self.update_signal.connect(self.update_ui)

    def update_ui(self, state):
        """Updates the UI elements with the new state."""
        self.label_ch.setText(f"CH: {state.get('channel', '-')}")
        self.label_aps.setText(f"APS: {state.get('aps', 0)}")
        self.label_up.setText(f"UP: {state.get('uptime', '00:00:00')}")

        face_key = state.get('face', 'neutral')
        self.label_face.setText(FACES.get(face_key, FACES['neutral']))

        self.label_pwnd.setText(f"PWND: {state.get('shakes', 0)}")
        self.label_mode.setText(f"{state.get('mode', 'MANU')}")
