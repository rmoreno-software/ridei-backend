package com.ridei.identity.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ridei.identity.domain.exception.InvalidProfilePictureUrlException;
import com.ridei.identity.domain.exception.ProfilePictureTooLargeException;
import com.ridei.identity.domain.model.Email;
import com.ridei.identity.domain.model.User;
import com.ridei.identity.domain.model.UserId;
import com.ridei.identity.domain.port.out.ImageProcessorPort;
import com.ridei.identity.domain.port.out.ProfilePictureStoragePort;
import com.ridei.identity.domain.port.out.UserRepositoryPort;

class ConfirmProfilePictureServiceTest {

    private static final long FIVE_MB = 5L * 1024 * 1024;
    private static final String UPLOADED_URL = "https://cdn/profile-pictures/u/original.png";

    private final UserRepositoryPort userRepository = mock(UserRepositoryPort.class);
    private final ProfilePictureStoragePort storage = mock(ProfilePictureStoragePort.class);
    private final ImageProcessorPort imageProcessor = mock(ImageProcessorPort.class);
    private final ConfirmProfilePictureService service =
        new ConfirmProfilePictureService(userRepository, storage, imageProcessor);

    private User user;
    private ConfirmProfilePictureCommand command;

    @BeforeEach
    void setUp() {
        user = User.register(new Email("rider@example.com"), "hash");
        command = new ConfirmProfilePictureCommand(user.getId(), UPLOADED_URL);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(storage.belongsToUser(user.getId(), UPLOADED_URL)).thenReturn(true);
    }

    @Test
    void pictureWithinLimitIsSavedAsIs() {
        when(storage.getContentLength(UPLOADED_URL)).thenReturn(FIVE_MB);

        service.confirm(command);

        assertThat(user.getPictureUrl()).isEqualTo(UPLOADED_URL);
        verify(userRepository).update(user);
        verify(storage, never()).delete(any());
    }

    @Test
    void oversizedPictureIsReplacedByCompressedCopy() {
        when(storage.getContentLength(UPLOADED_URL)).thenReturn(FIVE_MB + 1);
        when(storage.download(UPLOADED_URL)).thenReturn(new byte[10]);
        when(imageProcessor.compressToFit(any(), anyLong())).thenReturn(new byte[5]);
        when(storage.uploadProcessed(eq(user.getId()), any(), eq("jpg"), eq("image/jpeg")))
            .thenReturn("https://cdn/profile-pictures/u/compressed.jpg");

        service.confirm(command);

        assertThat(user.getPictureUrl()).isEqualTo("https://cdn/profile-pictures/u/compressed.jpg");
        verify(storage).delete(UPLOADED_URL);
        verify(userRepository).update(user);
    }

    @Test
    void pictureThatCannotBeCompressedEnoughIsDeletedAndRejected() {
        when(storage.getContentLength(UPLOADED_URL)).thenReturn(FIVE_MB + 1);
        when(storage.download(UPLOADED_URL)).thenReturn(new byte[10]);
        when(imageProcessor.compressToFit(any(), anyLong())).thenReturn(new byte[(int) FIVE_MB + 1]);

        assertThatThrownBy(() -> service.confirm(command)).isInstanceOf(ProfilePictureTooLargeException.class);

        verify(storage).delete(UPLOADED_URL);
        verify(userRepository, never()).update(any());
    }

    @Test
    void urlOutsideUserFolderIsRejected() {
        ConfirmProfilePictureCommand foreign = new ConfirmProfilePictureCommand(UserId.newId(), UPLOADED_URL);

        assertThatThrownBy(() -> service.confirm(foreign)).isInstanceOf(InvalidProfilePictureUrlException.class);
        verify(userRepository, never()).update(any());
    }
}
