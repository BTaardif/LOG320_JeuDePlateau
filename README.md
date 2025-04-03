Introduction
------------------------

Le jeu de tic-tac-toe, aussi nommé « morpion » en France et « oxo » en Belgique, est bien connu. Ce jeu de réflexion 
à deux joueurs se joue sur un plateau de 3x3 et le but consiste à aligner 3 pièces du même joueur de façon 
horizontale, verticale ou diagonale. Il est relativement simple pour un ordinateur de jouer parfaitement et ainsi 
gagner ou annuler à toutes les parties. Dans le cadre de ce laboratoire, nous allons utiliser une version plus complexe 
dans laquelle chaque case du tic-tac-toe est un autre plateau de tic-tac-toe. Il s’agit donc de deux plateaux imbriqués. 
Ceci rend l’arbre de recherche plus complexe et une stratégie plus complexe, incluant une vue globale du jeu, doit 
être définie.
Les objectifs de ce laboratoire sont :
• Implémentation de l’algorithme minimax et alpha-beta
• Implémentation d’heuristiques pour le jeu de Tic tac toe géant
• Exploration et implémentation de différentes stratégies afin d’améliorer l’efficacité de votre algorithme de 
décision.
