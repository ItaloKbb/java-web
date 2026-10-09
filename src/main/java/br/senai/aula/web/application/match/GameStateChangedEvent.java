package br.senai.aula.web.application.match;

/** Publicado sempre que uma ação altera a partida; ouvintes reagem após o commit. */
public record GameStateChangedEvent(long gameId) {}
