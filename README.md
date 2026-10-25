# ApexGarage pour Android

Application de garage (santé des pièces, entretien, pleins, budget, stock, diagnostic) qui fonctionne **hors ligne** sur ton téléphone. Elle n'a besoin d'aucune permission.

## Récupérer l'APK

À chaque modification envoyée sur ce dépôt, GitHub fabrique l'APK tout seul (5 à 8 minutes).

1. Onglet **Actions** : attends la coche verte sur la dernière ligne « Fabriquer l'APK ».
2. Page principale du dépôt, à droite : **Releases**, puis **ApexGarage (dernière version)**.
3. Télécharge **ApexGarage.apk** sur ton téléphone (connecte-toi à GitHub dans le navigateur, le dépôt est privé).
4. Ouvre le fichier et touche **Installer**. Autorise l'installation depuis le navigateur si Android le demande. Si Play Protect avertit, choisis **Installer quand même**.

Pour relancer la fabrication sans rien modifier : **Actions**, **Fabriquer l'APK**, **Run workflow**.

## Mettre à jour l'application petit à petit

- **Avec Claude** : décris le changement voulu, Claude modifie les fichiers du dépôt, GitHub fabrique le nouvel APK.
- **À la main** : ouvre `app/src/main/assets/index.html` sur GitHub, clique sur le crayon, modifie, puis **Commit changes**.

Installe chaque nouvel APK **par-dessus** l'ancien, sans désinstaller. Tes données sont conservées, car toutes les versions sont signées avec la même clé et le numéro de version monte tout seul.

## À ne pas oublier

- **Fais une sauvegarde régulière** : onglet Atelier, Données, **Exporter une sauvegarde**. Le fichier JSON va dans Téléchargements.
- **Garde ce dépôt privé.** La clé de signature (`keystore/apexgarage.jks`) y est enregistrée. Si tu la perds ou la changes, Android refusera d'installer les nouvelles versions par-dessus l'ancienne : il faudra désinstaller (en exportant d'abord ta sauvegarde).
- L'APK est signé avec cette clé personnelle. Il s'installe très bien, mais ce n'est pas un fichier prêt pour le Play Store.

## Où est quoi

| Élément | Rôle |
|---|---|
| `app/src/main/assets/index.html` | L'application : écrans, calculs, stockage. C'est le fichier à modifier le plus souvent. |
| `app/src/main/assets/fonts/` | Les polices, embarquées pour le hors ligne |
| `app/src/main/java/com/apexgarage/app/MainActivity.kt` | La coque Android : plein écran, bouton retour, export et import de sauvegarde |
| `app/src/main/res/` | Icône, couleurs, thème clair et sombre |
| `app/build.gradle.kts` | Réglages de l'application et de la signature |
| `keystore/apexgarage.jks` | La clé de signature |
| `.github/workflows/apk.yml` | La fabrication automatique de l'APK |

## Fabriquer sur ordinateur (facultatif)

Ouvre ce dossier dans Android Studio, attends la synchronisation, puis **Build**, **Build APK(s)**. L'APK sort dans `app/build/outputs/apk/`.
