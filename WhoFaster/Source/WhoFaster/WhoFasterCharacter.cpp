// Fill out your copyright notice in the Description page of Project Settings.


#include "WhoFasterCharacter.h"

// Sets default values
AWhoFasterCharacter::AWhoFasterCharacter()
{
 	// Set this character to call Tick() every frame.  You can turn this off to improve performance if you don't need it.
	PrimaryActorTick.bCanEverTick = true;

}

// Called when the game starts or when spawned
void AWhoFasterCharacter::BeginPlay()
{
	Super::BeginPlay();
	
}

// Called every frame
void AWhoFasterCharacter::Tick(float DeltaTime)
{
	Super::Tick(DeltaTime);

}

// Called to bind functionality to input
void AWhoFasterCharacter::SetupPlayerInputComponent(UInputComponent* PlayerInputComponent)
{
	Super::SetupPlayerInputComponent(PlayerInputComponent);

}

void AWhoFasterCharacter::OpenLobby()
{
	
}

void AWhoFasterCharacter::CallOpenLevel(const FString& Address)
{
}

void AWhoFasterCharacter::CallClientTravel(const FString& Address)
{
}

